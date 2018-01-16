package com.vmware.wavefront.metrics.wf_metrics;

import com.codahale.metrics.Gauge;

public class ObjectGauge implements Gauge 
{
	Object obj;
	public ObjectGauge(Object obj)
	{
		this.obj = obj;
	}
	
	public Object getValue() 
	{	
		return obj;
	}

	// returns the curent value.
	public void setValue(Object obj)
	{
		this.obj = obj;
	}
}
