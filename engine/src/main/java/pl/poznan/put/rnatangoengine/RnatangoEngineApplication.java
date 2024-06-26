package pl.poznan.put.rnatangoengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

@SpringBootApplication
@EnableSpringConfigured
@EnableAutoConfiguration(
    exclude = {org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class})
public class RnatangoEngineApplication {
  public static void main(String[] args) {
    SpringApplication.run(RnatangoEngineApplication.class, args);
  }
}
