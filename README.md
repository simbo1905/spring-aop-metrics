
### Demo Of spring-aop Adding Metrics About Method Execution Time

This demo reuses the excellent Spring Data JPA example from Baeldung. The original readme with links to the articles
are included below.

This repo adds a demo of using Spring AOP to add metrics about method execution time around spring-jpa and services. 
The context is that you have an existing modern spring application and would like to log statics about 
method timings and counts. This allows you to understand in a complex application where time is being spent. 

Note that Hibernate can be configured to log slow queries which is great. Yet you might find that you have 
services that are making a lot of calls that are themselves not slow yet are adding up to a lot of time. 

If you run the integration tests you will see that the metrics are logged on shutdown else every 15 minutes as: 

```text
[2024-10-08 07:55:33,126]-[SpringApplicationShutdownHook] INFO  metrics - type=HISTOGRAM, name=org.springframework.data.repository.CrudRepository.deleteAll, count=20, min=4, max=28, mean=9.888833584181375, stddev=4.927871930452384, p50=9.0, p75=11.0, p95=15.0, p98=28.0, p99=28.0, p999=28.0
[2024-10-08 08:22:58,587]-[main] INFO  metrics - type=HISTOGRAM, name=com.baeldung.jpa.simple.service.FooService.create, count=1, min=106, max=106, mean=106.0, stddev=0.0, p50=106.0, p75=106.0, p95=106.0, p98=106.0, p99=106.0, p999=106.0
[2024-10-08 08:22:58,587]-[main] INFO  metrics - type=HISTOGRAM, name=org.springframework.data.repository.CrudRepository.save, count=1, min=87, max=87, mean=87.0, stddev=0.0, p50=87.0, p75=87.0, p95=87.0, p98=87.0, p99=87.0, p999=87.0
```

In order to add metrics over the top of the existing spring-jpa and services we use Spring AOP by adding into the pom.xml:

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
```

We then have to tell the main appication to scan for the aspect:

```java
@SpringBootApplication
@EnableJpaRepositories("com.baeldung.jpa.simple.repository")
@EnableAspectJAutoProxy // <-- ADD THIS
public class JpaApplication {
    public static void main(String[] args) {
        SpringApplication.run(JpaApplication.class, args);
    }
}
```

The Aspect is the class MethodTimingAspect that is annotated with @Aspect and @Component. The @Around annotation is used
to record the histogram of the method execution times as per the sample output above. In the following 
only the key code is shown see the actual java for the details: 

```java
@Aspect
@Component
@Slf4j
public class MethodTimingAspect {

  @Around("execution(* com.baeldung.jpa..*.*(..))")
  public Object measureMethodExecutionTime(final ProceedingJoinPoint joinPoint) throws Throwable {
    final long startTime = System.currentTimeMillis();
    final Object result = joinPoint.proceed();
    final long executionTime = System.currentTimeMillis() - startTime;

    final String className = joinPoint.getSignature().getDeclaringTypeName();
    final String methodName = joinPoint.getSignature().getName();
    final String histogramName = className + "." + methodName;

    meterRegistry.histogram(histogramName).update(executionTime);

    log.trace("Duration was {} ms for {}", executionTime, histogramName);

    return result;
  }
}
```

Everthing below is the original readme from the Baeldung demo app which is over at https://github.com/eugenp/tutorials/tree/master/persistence-modules/spring-data-jpa-simple

### Spring Data JPA Articles that are also part of the e-book

From https://github.com/eugenp/tutorials copyright Baeldung

This module contains articles about Spring Data JPA that are also part of an Ebook.


### NOTE: 

Since this is a module tied to an e-book, it should **not** be moved or used to store the code for any further article.

### Relevant Articles
- [Introduction to Spring Data JPA](https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa)
- [Customizing the Result of JPA Queries with Aggregation Functions](https://www.baeldung.com/jpa-queries-custom-result-with-aggregation-functions)
- [CrudRepository, JpaRepository, and PagingAndSortingRepository in Spring Data](https://www.baeldung.com/spring-data-repositories)
- [New CRUD Repository Interfaces in Spring Data 3](https://www.baeldung.com/spring-data-3-crud-repository-interfaces)
- [Derived Query Methods in Spring Data JPA Repositories](https://www.baeldung.com/spring-data-derived-queries)
- [Spring Data JPA @Query](https://www.baeldung.com/spring-data-jpa-query)
- [Spring Data JPA Projections](https://www.baeldung.com/spring-data-jpa-projections)
- [Spring Data JPA @Modifying Annotation](https://www.baeldung.com/spring-data-jpa-modifying-annotation)
- [Generate Database Schema with Spring Data JPA](https://www.baeldung.com/spring-data-jpa-generate-db-schema)
- [Pagination and Sorting using Spring Data JPA](https://www.baeldung.com/spring-data-jpa-pagination-sorting)