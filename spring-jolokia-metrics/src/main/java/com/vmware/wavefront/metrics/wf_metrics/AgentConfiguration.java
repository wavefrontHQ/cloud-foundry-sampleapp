package com.vmware.wavefront.metrics.wf_metrics;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;

@Component
@ConfigurationProperties(prefix="wavefront")
public class AgentConfiguration 
{	
	protected Proxy proxy = new Proxy();
	protected Jolokia jolokia = new Jolokia();
	protected Agent agent = new Agent();
	
	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public Jolokia getJolokia() {
		return jolokia;
	}

	public void setJolokia(Jolokia jolokia) {
		this.jolokia = jolokia;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}
	
	public static class Agent
	{
		int retry;
		public int getRetry() {
			return retry;
		}

		public void setRetry(int retry) {
			this.retry = retry;
		}

		int interval;

		public int getInterval() {
			return interval;
		}

		public void setInterval(int interval) {
			this.interval = interval;
		}
	}

	public static class Proxy 
	{
		String host;
		int port;
		String mode;
		String servicename;
		
		public String getServicename() {
			return servicename;
		}
		public void setServicename(String servicename) {
			this.servicename = servicename;
		}
		public String getMode() {
			return mode;
		}
		public void setMode(String mode) {
			this.mode = mode;
		}
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
	}
	
	public static class Jolokia
	{
		String prefix;
		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		String baseurl;
		public String getBaseurl() {
			return baseurl;
		}

		public void setBaseurl(String baseurl) {
			this.baseurl = baseurl;
		}

		String endpoint;
		public String getEndpoint() {
			return endpoint;
		}

		public void setEndpoint(String endpoint) {
			this.endpoint = endpoint;
		}

		List<Metric> metrics = new ArrayList<Metric>();
		
		public List<Metric> getMetrics() {
			return metrics;
		}

		public void setMetrics(List<Metric> metrics) {
			this.metrics = metrics;
		}

		public static class Metric
		{
			String name;
			String mbean;
			List<String> attribute = new ArrayList<String>();
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public String getMbean() {
				return mbean;
			}
			public void setMbean(String mbean) {
				this.mbean = mbean;
			}
			public List<String> getAttribute() {
				return attribute;
			}
			public void setAttribute(List<String> attribute) {
				this.attribute = attribute;
			}
		}
	}
}
