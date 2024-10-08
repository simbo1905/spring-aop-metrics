package com.baeldung.jpa.simple;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

@SpringBootApplication
@EnableJpaRepositories("com.baeldung.jpa.simple.repository")
@EnableAspectJAutoProxy
public class JpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaApplication.class, args);
    }
  @Autowired
  private DataSource dataSource;

  //@Autowired
  private MetricRegistry metricRegistry = new MetricRegistry();

  @PostConstruct
  public void configureMetrics() {
    if (dataSource instanceof HikariDataSource) {
      ((HikariDataSource) dataSource).setMetricRegistry(metricRegistry);
    }
  }
}
