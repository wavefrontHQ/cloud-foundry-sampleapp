package springboot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit; 

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.CachePublicMetrics;
import org.springframework.boot.actuate.endpoint.DataSourcePublicMetrics;
import org.springframework.boot.actuate.endpoint.MetricReaderPublicMetrics;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.endpoint.SystemPublicMetrics;
import org.springframework.boot.actuate.endpoint.TomcatPublicMetrics;
import org.springframework.stereotype.Component;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.wavefront.integrations.metrics.WavefrontReporter;

@Component 
public class MetricSystem { 

    @Autowired 
    private MetricRegistry metricRegistry; 
    @Autowired
    private SystemPublicMetrics systemPublicMetrics;
    @Autowired
    private CachePublicMetrics cachePublicMetrics;
    @Autowired
    private DataSourcePublicMetrics dataSourcePublicMetrics;
    @Autowired
    private MetricReaderPublicMetrics metricReaderPublicMetrics;
    @Autowired
    private TomcatPublicMetrics tomcatPublicMetrics;

    // create some new metrics which can be reported
    private Counter writeRequestCount;
    private Long lastWriteTime;

    @PostConstruct
    public void init() {
        writeRequestCount = metricRegistry.counter("writeRequestCount");
        metricRegistry.register("lastWriteTime", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return lastWriteTime;
            }
        });

        addMetrics(systemPublicMetrics);
        addMetrics(cachePublicMetrics);
        addMetrics(dataSourcePublicMetrics);
        addMetrics(metricReaderPublicMetrics);
        addMetrics(tomcatPublicMetrics);

        // check the env variables for services
        String services = System.getenv("VCAP_SERVICES");
        System.out.println("VCAP_SERVICES = " + services);
        if (StringUtils.isNotBlank(services)) {
            JSONObject json = new JSONObject(services);

            JSONArray jsonArray = json.getJSONArray("wavefront-proxy");
            JSONObject details = jsonArray.getJSONObject(0);
            System.out.println("Details = " + details);

            JSONObject credentials = details.getJSONObject("credentials");
            String hostname = credentials.getString("hostname");
            int port = credentials.getInt("port");
            System.out.println("Hostname = " + hostname);
            System.out.println("Port = " + port);
            // send metrics to wavefront
            WavefrontReporter wfReporter = WavefrontReporter.forRegistry(metricRegistry)
                    .withSource("springboot")
                    .prefixedWith("springboot3")
                    .build(hostname, port);
            wfReporter.start(10, TimeUnit.SECONDS);
        } else {
            System.out.println("VCAP_SERVICES not found in env variables");
            System.exit(-1);
        }
    }

    private void addMetrics(PublicMetrics metrics) {
	metricRegistry.register("spring.boot", (MetricSet) () -> {
            final Map<String, Metric> gauges = new HashMap<String, Metric>();

            for (final org.springframework.boot.actuate.metrics.Metric<?> springMetric : 
                    metrics.metrics()) {
        	
                gauges.put(springMetric.getName(), (Gauge<Object>) () -> {

                    return metrics.metrics().stream()
                        .filter(m -> StringUtils.equals(m.getName(), springMetric.getName()))
                        .map(m -> m.getValue())
                        .findFirst()
                        .orElse(null);

                });
            }
            return Collections.unmodifiableMap(gauges);
        });
    } 

    public Counter sampleAppWriteRequestCount() {
	return writeRequestCount;
    }

    public void markSampleAppLastWriteTime() {
        lastWriteTime = System.currentTimeMillis();
    } 
    
    public void incSampleAppWriteCount() {
	writeRequestCount.inc();
    }
} 
