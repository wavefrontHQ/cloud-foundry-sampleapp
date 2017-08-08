package repository;

import java.util.ArrayList; 
import java.util.Collections; 
import java.util.List; 

import org.springframework.stereotype.Repository;

import domain.SampleAppData;

@Repository 
public class SampleAppRepository {

    private List<SampleAppData> dataList = new ArrayList<SampleAppData>();

    public void addData(SampleAppData appData) {
	dataList.add(appData);
    }

    public List<SampleAppData> getAllData() {
        return Collections.unmodifiableList(dataList);
    } 
} 
