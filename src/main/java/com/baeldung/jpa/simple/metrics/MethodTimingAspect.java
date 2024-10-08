package com.baeldung.jpa.simple.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class MethodTimingAspect {

  final MetricRegistry meterRegistry;
  final Slf4jReporter reporter;

  /**
   * This ensures that we log our metrics to the console on shutdown.
   */
  @PreDestroy
  void shutdownReporter() {
    reporter.stop();
  }

  public MethodTimingAspect() {
    this.meterRegistry = new MetricRegistry();
    meterRegistry.register("gc", new GarbageCollectorMetricSet());
    meterRegistry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
    this.reporter = Slf4jReporter.forRegistry(meterRegistry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .outputTo(org.slf4j.LoggerFactory.getLogger("metrics"))
        .build();
    reporter.start(15, TimeUnit.MINUTES);
  }

  @Around("execution(* com.baeldung.jpa..*.*(..))")
  public Object measureMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.currentTimeMillis();
    Object result = joinPoint.proceed();
    long executionTime = System.currentTimeMillis() - startTime;

    String className = joinPoint.getSignature().getDeclaringTypeName();
    String methodName = joinPoint.getSignature().getName();
    String histogramName = className + "." + methodName;

    meterRegistry.histogram(histogramName)
        .update(executionTime);

    log.trace("Duration was {} ms for {}", executionTime, histogramName);

    return result;
  }
}
