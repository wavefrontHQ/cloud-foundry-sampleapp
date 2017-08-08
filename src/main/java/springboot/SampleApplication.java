package springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"springboot", "controller", "service", "repository", "domain"})
public class SampleApplication {

  @Autowired
  private MetricSystem metricSystem;

  public static void main(String[] args) {
    SpringApplication.run(SampleApplication.class, args);
  }
} 
