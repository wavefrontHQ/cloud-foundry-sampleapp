package com.vmware.wavefront.metrics.wf_metrics;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.wavefront.util.ASCIIArtsLogger;
import com.wavefront.integrations.metrics.WavefrontReporter;


@Component
@EnableAutoConfiguration
public class SpringbootWavefrontMetricsAgent extends AbstractMetricsAgent
{
	@Autowired
	protected AgentConfiguration config;
	
	@Autowired
	JolokiaMetricSet metricSet;
	
	protected ObjectMapper mapper;
	protected MetricRegistry registry;
	protected WavefrontReporter reporter;
	protected String proxyHost;
	protected int proxyPort;
	
	private static final Logger logger = LoggerFactory.getLogger(SpringbootWavefrontMetricsAgent.class);
	boolean run;
	
	@PostConstruct
	public void init()
	{
		mapper = new ObjectMapper();
		
		ASCIIArtsLogger.logWavefront(logger);
		logger.info(" ");
		logger.info(" Springboot Wavefront Metrics Agent");
		logger.info("====================================");
		
		proxyHost = config.getProxy().getHost();
		proxyPort = config.getProxy().getPort();
		
		try
		{
			logger.info("[config]" + mapper.writeValueAsString(config));
		}
		catch(Exception e)
		{
			logger.error("[exception]" + e.getMessage(), e);
		}
		
		// define metrics first
		registry = new MetricRegistry();
		
		// start the run thread.
		this.start();
	}

	@Override
	public void run() 
	{
		boolean isAvailable = false;
		String baseUrl = config.getJolokia().getBaseurl();
		String endpoint = config.getJolokia().getEndpoint();
		int retry = config.getAgent().getRetry();
		
		int tryCount = 0;
		while(isAvailable == false)
		{
			if(tryCount == retry)
			{
				logger.info("Maximum retry count to find local jolokia has reached without success. Seems agent cannot continue, halting...");
				break;
			}
			
			try
			{
				Thread.sleep(1000);
				
				String json = null;
				try
				{
					json = getAPI(baseUrl, null, endpoint);
				}
				catch(Exception e)
				{
					logger.error("[error]" + e.getMessage());
					// suppress any errors.
				}
				
				if(json != null)
				{
					JSONObject jolokia = (JSONObject)parser.parse(json);
					
					logger.info("[json]" + jolokia.toJSONString());
					
					int status = ((Number)jolokia.get("status")).intValue();
					
					logger.info("[status]" + status);
					
					if(status == 200)
					{
						// get source and point tags necessary to start wavefront reporter
						JSONObject value = (JSONObject)jolokia.get("value");
						String agent = (String)value.get("agent");
						String protocol = (String)value.get("protocol");
						JSONObject _config = (JSONObject)value.get("config");
						
						String agentId = (String)_config.get("agentId");
						String agentType = (String)_config.get("agentType");
						
						JSONObject info = (JSONObject)value.get("info");
						String product = (String)info.get("product");
						String vendor = (String)info.get("vendor");
						String version = (String)info.get("version");
						
						logger.info("[WF reporter]Creating reporter...");
						
						// set registry with jolokai one
						registry.register(config.getJolokia().getPrefix(), metricSet);
						
						if(config.getProxy().getMode().equals("pcf"))
						{
							reporter = WavefrontReporter.forRegistry(registry)
								    .withSource(agentId)
								    	.withPointTag("agent", agent)
								    	.withPointTag("protocol", protocol)
								    	.withPointTag("product", product)
								    	.withPointTag("vendor", vendor)
								    	.withPointTag("version", version)
								    	.withJvmMetrics()
								    	.bindToCloudFoundryService(config.getProxy().getServicename(), true);
						}
						else
						{
							// start up the wavefront reporter
							reporter = WavefrontReporter.forRegistry(registry)
							    .withSource(agentId)
							    	.withPointTag("agent", agent)
							    	.withPointTag("protocol", protocol)
							    	.withPointTag("product", product)
							    	.withPointTag("vendor", vendor)
							    	.withPointTag("version", version)
							    	.withJvmMetrics()
							    	.build(proxyHost, proxyPort);
						}
						
						// start the reporter
						// report will start sending the collected metrics every given interval
						// defined in the config.
						reporter.start(config.getAgent().getInterval(), TimeUnit.SECONDS);
						isAvailable = true;
						
						logger.info("[WF reporter]Started...");
					}
				}
			}
			catch(Exception e)
			{
				logger.error("[error]" + e.getMessage(), e);
			}
			tryCount++;
		}
		
		/**
		 * running and collecting part.
		 */
		while(isAvailable)
		{
			try
			{
				Thread.sleep(config.getAgent().getInterval() * 1000);
				// refresh the metrics after given interval
				reload(registry);
			}
			catch(Exception e)
			{
				logger.error("[error]" + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * reload the metrics
	 */
	public void reload(MetricRegistry registry)
	{
		logger.debug("[load]begin...");
		
		// clear the map first.
		
		Iterator itr = config.getJolokia().getMetrics().iterator();
		while(itr.hasNext())
		{
			AgentConfiguration.Jolokia.Metric metric = (AgentConfiguration.Jolokia.Metric)itr.next();
			String name = metric.getName();
			String mbean = metric.getMbean();
			List<String> attributes = metric.getAttribute();
			
			try
			{
				String json = getAPI(config.getJolokia().getBaseurl(), null, config.getJolokia().getEndpoint() + "read/" + URLEncoder.encode(mbean, "UTF-8").replaceAll("\\+", "%20"));
				logger.debug("[json]" + json);
				JSONObject response = (JSONObject)parser.parse(json);
				JSONObject value = (JSONObject)response.get("value");
				
				for(String attribute : attributes)
				{
					Object obj = value.get(attribute);
					if(obj instanceof JSONObject)
					{
						JSONObject jsonObj = (JSONObject)obj;
						Iterator keyItr = jsonObj.keySet().iterator();
						while(keyItr.hasNext())
						{
							Object keyVal = keyItr.next();
							Object valObj = jsonObj.get(keyVal);
							if(valObj instanceof Number)
							{
								String metricName = name + "." + keyVal;
								Number metricValue = (Number)valObj;
								ObjectGauge gauge = (ObjectGauge)registry.getGauges().get(config.getJolokia().getPrefix() + "." + metricName);
								if(gauge != null)
								{
									gauge.setValue(metricValue);
								}
							}
						}
					}
					else if(obj instanceof Number)
					{
						String metricName = name + "." + attribute;
						Number metricValue = (Number)obj;
						
						ObjectGauge gauge = (ObjectGauge)registry.getGauges().get(config.getJolokia().getPrefix() + "." + metricName);
						if(gauge != null)
						{
							gauge.setValue(metricValue);
						}
					}
				}
			}
			catch(Exception e)
			{
				logger.error("[error]" + e.getMessage(), e);
			}
		}
		logger.debug("[load]end...");
	}
}
