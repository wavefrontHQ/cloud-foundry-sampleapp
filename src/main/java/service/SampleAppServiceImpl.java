package service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import domain.SampleAppData;
import repository.SampleAppRepository;

import javax.sql.DataSource;

@Service 
public class SampleAppServiceImpl implements SampleAppService {

  @Autowired 
  private SampleAppRepository repository;
  
  @Autowired
  private DataSource ds;

  public SampleAppData create(SampleAppData geolocation) {
   repository.addData(geolocation);
   return geolocation; 
  }

  public List<SampleAppData> findAll() {
    return repository.getAllData();
  } 
  
  @Cacheable("guavaCachesearches")
  public List<SampleAppData> fetch(String searchType, String keyword) {
      return Lists.newArrayList();
  }
}
