package com.telecom.provisioning.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS for local Angular development (http://localhost:4200).
 *
 * <p>Live UI integration arrives in Phase 9; this foundation entry only
 * ensures the browser can call the APIs once screens exist. The
 * correlation header is exposed so the UI can read it back for tracing.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins("http://localhost:4200")
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("X-Correlation-ID")
        .maxAge(3600);
  }
}
