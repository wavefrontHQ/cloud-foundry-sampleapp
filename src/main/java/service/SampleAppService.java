package service;

import java.util.List;

import domain.SampleAppData;

public interface SampleAppService {

    public SampleAppData create(SampleAppData geolocation);
    public List<SampleAppData> findAll();
}  
