package springboot;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * This class is responsible for identifying the metrics and sending them to wavefront.
 */
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

  @Value("${spring.application.instance_id}")
  private String instanceId;

  // create some new metrics which can be reported
  private Counter writeRequestCount;
  private List<Counter> coolRequesCount;
  private Long lastWriteTime;

  @PostConstruct
  public void init() {
    // create new application level metrics and add them to metricRegistry
    writeRequestCount = metricRegistry.counter("writeRequestCount");
    coolRequesCount = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      String name = "readRequestMark" + i;
      System.out.println("The name is: " + name);
      coolRequesCount.add(metricRegistry.counter(name));
    }
    metricRegistry.register("lastWriteTime", (Gauge<Long>) () -> lastWriteTime);
    // add the system metrics, which needs to be forwarded to wavefront, to metricRegistry
    addMetrics(systemPublicMetrics);
    addMetrics(cachePublicMetrics);
    addMetrics(dataSourcePublicMetrics);
    addMetrics(metricReaderPublicMetrics);
    addMetrics(tomcatPublicMetrics);

    // send all the metrics registered in metricRegistry to wavefront
    WavefrontReporter wfReporter =
        WavefrontReporter.forRegistry(metricRegistry)
                         .withSource("springboot")
                         .prefixedWith("pcf")
                .withPointTag("processId", instanceId)
                         .bindToCloudFoundryService("wavefront-proxy", true);
    wfReporter.start(10, TimeUnit.SECONDS);
  }

  private void addMetrics(PublicMetrics metrics) {
    metricRegistry.register("spring.boot", (MetricSet) () -> {
      final Map<String, Metric> metricsMap = new HashMap<String, Metric>();

      for (final org.springframework.boot.actuate.metrics.Metric<?> metric :
          metrics.metrics()) {

        metricsMap.put(metric.getName(), (Gauge<Object>) () -> {

          return metrics.metrics().stream()
                        .filter(m -> StringUtils.equals(m.getName(), metric.getName()))
                        .map(m -> m.getValue())
                        .findFirst()
                        .orElse(null);

        });
      }
      return Collections.unmodifiableMap(metricsMap);
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
  public void incSampleAppReadCount() {
    coolRequesCount.forEach(Counter::inc);
  }
}
