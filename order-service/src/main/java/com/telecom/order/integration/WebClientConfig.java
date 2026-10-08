package com.telecom.order.integration;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/** One WebClient per downstream service, all propagating X-Correlation-ID. */
@Configuration
public class WebClientConfig {

  private final CorrelationPropagationFilter propagation;
  private final int timeoutSeconds;

  public WebClientConfig(CorrelationPropagationFilter propagation,
      @Value("${integration.timeout-seconds:5}") int timeoutSeconds) {
    this.propagation = propagation;
    this.timeoutSeconds = timeoutSeconds;
  }

  @Bean
  public WebClient customerWebClient(@Value("${integration.customer-service-url}") String baseUrl) {
    return baseClient(baseUrl);
  }

  @Bean
  public WebClient inventoryWebClient(@Value("${integration.inventory-service-url}") String baseUrl) {
    return baseClient(baseUrl);
  }

  @Bean
  public WebClient provisioningWebClient(@Value("${integration.provisioning-service-url}") String baseUrl) {
    return baseClient(baseUrl);
  }

  @Bean
  public WebClient notificationWebClient(@Value("${integration.notification-service-url}") String baseUrl) {
    return baseClient(baseUrl);
  }

  private WebClient baseClient(String baseUrl) {
    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofSeconds(timeoutSeconds));
    return WebClient.builder()
        .baseUrl(baseUrl)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .filter(propagation.filter())
        .build();
  }
}
