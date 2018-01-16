# spring-actuator-metrics
Wavefront Cloud Foundry (Spring Boot) Sample Application which collects spring boot actuator metrics.

This project describes how to send metrics from a springboot app running on pivotal cloud foundry to wavefront proxy.

## Wavefront Proxy

Wavefront proxy service has to be started on PCF. In the rest of the document we assume the wavefront proxy service is installed and available.

## Application Manifest (optional)

A manifest file is used to provide parameters to a PCF application. The parameters are provided in a file called `manifest.yml`. The developer needs to identify the wavefront proxy running in PCF and add it to the manifest file. Here is a sample manifest file:-

```
---
 services:
  - wavefront-proxy
```

* Including the manifest information is optional

## POM File

The pom file has to be updated to include the wavefrontHQ public repositories and dependencies (`dropwizard-metrics-3.1`). It should also include `spring-boot-maven-plugin` and `maven-shade-plugin`.

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

Please make sure that the "wavefront-proxy" exists inside the VCAP_SERVICES, and has correct hostname and port that is reachable via the springboot application.

## Send metrics from the application

Most of the code is present in `MetricSystem.java` file. It determines all the metrics, which have
 to be reported, and adds them to the `metricRegistry`. Then `WavefrontReporter` is used to send 
 the metrics to the proxy.

```
WavefrontReporter wfReporter = WavefrontReporter.forRegistry(metricRegistry)
    .withSource("springboot")
    .prefixedWith("pcf")
    .bindToCloudFoundryService("wavefront-proxy", true);
wfReporter.start(10,  TimeUnit.SECONDS);
```

The `wavefront-proxy` is the name of the wavefront proxy service running on PCF. The Wavefront 
tile in should be used to install the wavefront proxy service. Once the tile is installed, the 
default name of the wavefront proxy service will be `wavefront-proxy`. 

There are additional overloaded methods available in `WavefrontReporter` class, which can be used
 to bind to wavefront proxy (e.g., `bindToCloudFoundryService()`).
 
## Build and push the application

```
mvn clean install -DskipTests

cf login -a <pcf-api-url> --skip-ssl-validation --sso

cf push springboot  -f src/main/resources/manifest.yml -p target/springboot-0.0.1-SNAPSHOT.jar

cf logs springboot --recent

(If the manifest does not have wavefront-proxy service info, then the service can be bound to it 
later using the following commands.)
cf bind-service springboot wfproxy-service1
cf restage springboot
```

## Metrics collected

The sample application collects metrics that is available from springboot actuator, which is part of its dependency. More information can be found [here](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-metrics.html) regarding how the actuator works, and what kind of metrics can be gathered. Currenly, the following metrics are gathered from the sample applications and sent to wavefront proxy.

- System metrics
- DataSource metrics
- Cache metrics
- Tomcat session metrics
- Custom App Metrics

metrics will have source as "springboot," and will all be included under "pcf.spring.boot" metric space. The following metrics will be available:

- pcf.spring.boot.cache
- pcf.spring.boot.classes
- pcf.spring.boot.datasource
- pcf.spring.boot.gc
- pcf.spring.boot.heap
- pcf.spring.boot.httpsessions
- pcf.spring.boot.instance
- pcf.spring.boot.mem
- pcf.spring.boot.nonheap.
- pcf.spring.boot.processors
- pcf.spring.boot.systemload
- pcf.spring.boot.threads
- pcf.spring.boot.uptime 
