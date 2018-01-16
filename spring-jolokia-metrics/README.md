# spring-jolokia-metrics
Wavefront Cloud Foundry (Spring Boot) Sample Application for Jolokia

This project describes how to send metrics from a springboot app running on pivotal cloud foundry to wavefront proxy, collecting system metrics from embedded [jolokia](https://jolokia.org/) endpoint.

## Wavefront Proxy

Wavefront proxy service has to be started on PCF. In the rest of the document we assume the wavefront proxy service is called `wavefront-proxy`. It needs to be replaced with the service name appropriately.


## POM File

The pom file has to be updated to include the wavefrontHQ public repositories and dependencies. The pom.xml file of the module will automatically include the spring actuator and jolokia as part of its dependencies, so you don't have to include it in your pom.xml file.

```
<dependency>
  <groupId>com.vmware.wavefront.metrics</groupId>
  <artifactId>wf-metrics</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Parse VCAP_SERVICES

The VCAP_SERVICES present in the manifest file (or externally bound to the application using `cf bind-services ...` command) are passed to the application as environment variable. It should be parsed to retrieve the wavefront-proxy `hostname` and `port`

```
VCAP_SERVICES =
{
  "wavefront-proxy": [
    {
      "credentials": {
        "hostname": "tcp.wavefront.io",
        "port": 1099
      },
      "syslog_drain_url": null,
      "volume_mounts": [],
      "label": "wavefront-proxy",
      "provider": null,
      "plan": "standard",
      "name": "wfproxy-service1",
      "tags": [
        "wavefront",
        "metrics"
      ]
    }
  ]
}
```


## application.yml

Your springboot application's application.yml should have the following settings present in order for the wf-metrics to work properly. Make sure the servicename is present in your environment variables inside VCAP_SERVICES as 'wavefront-proxy.' Also, make sure the mode is set to 'pcf,' not 'standalone.'

```
# application properties
management:
  security:
    enabled: false
  jolokia:
    enabled: true
    config:
      debug: true
      
wavefront:
  agent:
    interval: 10
    retry: 5
  proxy:
    # [standalone|pcf]
    mode: pcf
    # used when mode is pcf
    servicename: wavefront-proxy
    host: localhost
    port: 2878
  jolokia:
    baseurl:  http://localhost:8080
    endpoint: /jolokia/
    prefix: jolokia
    metrics:
      -
        name: jvm.heap.memory.usage
        mbean: java.lang:type=Memory
        attribute:
          - HeapMemoryUsage
          - NonHeapMemoryUsage
      -
        name: jvm.thread.count
        mbean: java.lang:type=Threading
        attribute:
          - TotalStartedThreadCount
          - ThreadCount
          - DaemonThreadCount
          - PeakThreadCount
          - CurrentThreadUserTime
          - CurrentThreadCpuTime
      -
        name: jvm.class.count
        mbean: java.lang:type=ClassLoading
        attribute:
          - LoadedClassCount
          - UnloadedClassCount
          - TotalLoadedClassCount
      - 
        name: jvm.gc
        mbean: java.lang:name=PS MarkSweep,type=GarbageCollector
        attribute:
          - CollectionTime
          - CollectionCount
      -
        name: jvm.nio.direct
        mbean: java.nio:name=direct,type=BufferPool
        attribute:
          - TotalCapacity
          - MemoryUsed
          - Count
      -
        name: jvm.nio.mapped
        mbean: java.nio:name=mapped,type=BufferPool
        attribute:
          - TotalCapacity
          - MemoryUsed
          - Count
      - 
        name: tomcat.http_nio_8080
        mbean: Tomcat:name="http-nio-8080",type=GlobalRequestProcessor
        attribute:
          - requestCount
          - maxTime
          - byteReceived
          - byteSent
          - processingTime
          - errorCount
```

## Setting up component scan in your main application

In order for the wf-metrics to start up properly, you need to tell your springboot to scan for the components under "com.vmware.wavefront" package, as shown below, using @ComponentScan annotation.

```
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.vmware.wavefront.util.ASCIIArtsLogger;

@RestController
@SpringBootApplication
@ComponentScan("com.vmware.wavefront")
public class App 
{
  /**
   * simple App
   * @param args
   * @throws Exception
   */
    public static void main(String[] args) throws Exception 
    {
        SpringApplication.run(App.class, args);
    }
    
    /**
     * main entry point
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/", method=RequestMethod.GET, produces={"text/html"})
    String home() throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html>");
        buffer.append("<head><title>Wavefront Springboot Sample App</title></head>");
        buffer.append("<body>");
        
        buffer.append("<pre>");
        for(String line : ASCIIArtsLogger.logo)
        {
          buffer.append(line);
          buffer.append("\n");
        }
        buffer.append("</pre>");
        
        buffer.append("<p>Hello, this is a sample springboot application for wavefront integration. <br/>I'm glad that you are trying this out.</p>");
        
        buffer.append("</body>");
        buffer.append("</html>");
        return buffer.toString();
    }
}

```
 
## Running the application

When all the setups are properly done, you'll see the following log messages in your log output.

```
2018-01-15 17:47:55.289  INFO 89329 --- [           main] .v.w.m.w.SpringbootWavefrontMetricsAgent :                           __                 _   
2018-01-15 17:47:55.290  INFO 89329 --- [           main] .v.w.m.w.SpringbootWavefrontMetricsAgent :                          / _|               | |  
2018-01-15 17:47:55.290  INFO 89329 --- [           main] .v.w.m.w.SpringbootWavefrontMetricsAgent : __      ____ ___   _____| |_ _ __ ___  _ __ | |_
2018-01-15 17:47:55.290  INFO 89329 --- [           main] .v.w.m.w.SpringbootWavefrontMetricsAgent : \ \ /\ / / _` \ \ / / _ \  _| '__/ _ \| '_ \| __|
2018-01-15 17:47:55.290  INFO 89329 --- [           main] .v.w.m.w.SpringbootWavefrontMetricsAgent :  \ V  V / (_| |\ V /  __/ | | | | (_) | | | | |_ 
2018-01-15 17:47:55.290  INFO 89329 --- [           main] .v.w.m.w.SpringbootWavefrontMetricsAgent :   \_/\_/ \__,_| \_/ \___|_| |_|  \___/|_| |_|\__|
2018-01-15 17:47:55.290  INFO 89329 --- [           main] .v.w.m.w.SpringbootWavefrontMetricsAgent :  
2018-01-15 17:47:55.290  INFO 89329 --- [           main] .v.w.m.w.SpringbootWavefrontMetricsAgent :  Springboot Wavefront Metrics Agent
2018-01-15 17:47:55.290  INFO 89329 --- [           main] .v.w.m.w.SpringbootWavefrontMetricsAgent : ====================================
```

## Checking the metrics

As defined by default in application.yml, the metrics prefix is 'jolokia.' Look for the metrics starting with 'jolokia' in your wavefront UI's metrics browser, and you should be seeing the metrics flowing through. The source will be set as the jolokia servlet's agent ID, which is composed of IP address of the machine, and unique identification. The below metrics can be configured to either be added or removed from the generated metrics by editing your application.yml file.

- jolokia.jvm.class
- jolokia.jvm.gc
- jolokia.jvm.heap
- jolokia.jvm.nio
- jolokia.jvm.thread
- jolokia.tomcat.http_nio_8080

The following additional metrics are enabled as part of the wavefront dropwizard metrics reporter that collects java performance metrics.

- jvm.buffers
- jvm.classes
- jvm.current_time
- jvm.fd_usage
- jvm.gc
- jvm.memory
- jvm.thread-states
- jvm.uptime
