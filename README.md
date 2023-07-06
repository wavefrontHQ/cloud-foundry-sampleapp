> **Warning**
>
> This repository is no longer maintained.

# Wavefront Cloud Foundry Sample Spring Boot Application

This project describes how to send application metrics from a Spring Boot app running in PCF to a Wavefront proxy.

## Requirements
* A Wavefront proxy deployed in PCF as part of the [Wavefront by VMware Nozzle for PCF](https://network.pivotal.io/products/wavefront-nozzle/)
* A Wavefront proxy service instance provisioned in PCF using the [Wavefront Service Broker for PCF](http://docs.pivotal.io/partners/wavefront-nozzle/installing.html#marketplace).
* Java >= 1.8
* Maven

## Wavefront Proxy

The rest of this document assumes the wavefront proxy service instance provisioned in PCF is called `wfproxy-service`. Replace the service name accordingly if different.

## Application Manifest

A `manifest.yml` file is used to provide parameters to a PCF application. The developer needs to identify the wavefront proxy running in PCF and add it to the manifest file.

Here is a sample manifest file:

```
---
 services:
  - wfproxy-service
```

## Maven pom.xml File

Update the Maven `pom.xml` file to include the wavefrontHQ public repositories and dependencies `dropwizard-metrics-3.1`. It should also include `spring-boot-maven-plugin` and `maven-shade-plugin`.

## Parse VCAP_SERVICES

The `VCAP_SERVICES` present in the manifest file (or externally bound to the application using `cf bind-services ...` command) are passed to the application as environment variable. It is parsed to retrieve the wavefront-proxy `hostname` and `port`:

```
VCAP_SERVICES =
{
  "wavefront-proxy": [
    {
      "credentials": {
        "hostname": "10.202.1.15",
        "port": 2878
      },
      "syslog_drain_url": null,
      "volume_mounts": [],
      "label": "wavefront-proxy",
      "provider": null,
      "plan": "standard",
      "name": "wfproxy-service",
      "tags": [
        "wavefront",
        "metrics"
      ]
    }
  ]
}
```

## Send metrics from the application

Most of the sample code is present in the `MetricSystem.java` file. It determines all the metrics to be reported and adds them to the `metricRegistry`. The `WavefrontReporter` is then used to send the metrics to the proxy.

```
WavefrontReporter wfReporter = WavefrontReporter.forRegistry(metricRegistry)
    .withSource("springboot")
    .prefixedWith("pcf")
    .bindToCloudFoundryService("wavefront-proxy", true);
wfReporter.start(10,  TimeUnit.SECONDS);
```
The `wavefront-proxy` is the name of the wavefront proxy service running in PCF. The Wavefront
tile should be used to install the wavefront proxy service. Once the tile is installed, the
default name of the wavefront proxy service will be `wavefront-proxy`.

There are additional overloaded methods available in `WavefrontReporter` class, which can be used
 to bind to wavefront proxy (e.g., `bindToCloudFoundryService()`).

## Build and push the application

```
mvn clean install -DskipTests
cf login -a <pcf-api-url> --skip-ssl-validation --sso
cf push wavefront-sample-app -f src/main/resources/manifest.yml -p target/springboot-0.0.1-SNAPSHOT.jar
cf logs wavefront-sample-app --recent
```
If the manifest does not have wavefront-proxy service info, then the service can be bound to it
later using the following commands:
```
cf bind-service wavefront-sample-app wfproxy-service
cf restage wavefront-sample-app
```
