# cloud-foundry-sampleapp
Wavefront Cloud Foundry (Spring Boot) Sample Application

This project describes how to send metrics from a springboot app running on pivotal cloud foundry to wavefront proxy.

## Wavefront Proxy

Wavefront proxy service has to be started on PCF. In the rest of the document we assume the wavefront proxy service is called `wfproxy-service1`. It needs to be replaced with the service name appropriately.

## Application Manifest

A manifest file is used to provide parameters to a PCF application. The parameters are provided in a file called `manifest.yml`. The developer needs to identify the wavefront proxy running in PCF and add it to the manifest file. Here is a sample manifest file:-

```
---
 services:
  - wfproxy-service1
```

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

## Send metrics from the application

Determine all the metrics, which have to be reported, and add them to the `metricRegistry`. Then use the `WavefrontReporter` to send them to the proxy.

```
WavefrontReporter wfReporter = WavefrontReporter.forRegistry(metricRegistry)
    .withSource("springboot")
    .prefixedWith("springboot2")
    .build(hostname, port);
wfReporter.start(10,  TimeUnit.SECONDS);
```

## Build and push the application

```
mvn clean install -DskipTests

cf login -a <pcf-api-url> --skip-ssl-validation --sso

cf push springboot2  -f src/main/resources/manifest.yml -p target/springboot-0.0.1-SNAPSHOT.jar

cf logs springboot2 --recent

(If the manifest does not have wavefront-proxy service info, then the service can be bound to it 
later using the following commands.)
cf bind-service springboot2 wfproxy-service1
cf restage springboot2
```
