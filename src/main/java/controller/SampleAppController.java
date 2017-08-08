package controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import domain.SampleAppData;
import service.SampleAppService;
import springboot.MetricSystem; 

@RestController 
@RequestMapping("/sampleapp")
public class SampleAppController {
    @Autowired
    private SampleAppService service;

    @Autowired
    private MetricSystem metricSystem;

    @RequestMapping(method = RequestMethod.POST, produces = "application/json",
                    consumes = "application/json")
    public SampleAppData create(@RequestBody SampleAppData geolocation) {
        metricSystem.markSampleAppLastWriteTime();
        metricSystem.incSampleAppWriteCount();
        return service.create(geolocation);
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public List<SampleAppData> findAll() {
	return service.findAll();
    } 
} 
