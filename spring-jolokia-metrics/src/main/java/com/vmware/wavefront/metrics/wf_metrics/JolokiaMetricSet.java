package com.vmware.wavefront.metrics.wf_metrics;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JolokiaMetricSet extends AbstractMetricsAgent implements MetricSet 
{
	private static final Logger logger = LoggerFactory.getLogger(JolokiaMetricSet.class);
	protected final Map<String, Metric> gauges = new HashMap<String, Metric>();
	
	@Autowired
	AgentConfiguration config;
	
	/**
	 * retrieve jolokia metrics
	 */
	public Map<String, Metric> getMetrics() 
	{
		load();
		return gauges;
	}

	/**
	 * load the metrics
	 */
	public void load()
	{
		logger.debug("[load]begin...");
		
		// clear the map first.
		gauges.clear();
		
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
				logger.info("[json]" + json);
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
								
								ObjectGauge gauge = (ObjectGauge)gauges.get(metricName);
								if(gauge == null)
								{
									gauges.put(metricName, new ObjectGauge(metricValue));
								}
								else
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
						
						ObjectGauge gauge = (ObjectGauge)gauges.get(metricName);
						if(gauge == null)
						{
							gauges.put(metricName, new ObjectGauge(metricValue));
						}
						else
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
