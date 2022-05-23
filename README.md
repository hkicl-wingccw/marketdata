# Market Data Throttle Control

This project is to simluate receiving real time market data from exchange then sending them to other applications.

### Prerequisite
* Apache Maven 3.8.4
* JAVA - openjdk 18

### dependency
* lombok
* rxjava - 3.1.3
* spring-boot-starter
* spring-boot-starter-test

### Assumption
* Not to use `Guava RateLimitter` because of sync, blocking approach behind the scenes. and also no retry mechanism.
* Not to use `Resilience4j Ratelimiter` because of hard to distinct symbol from it.


### Unit test
```bash
# run all test cases in MaketDataProcessorTest.java
mvn test

# for particular unit test case
mvn test -Dtest=MaketDataProcessorTest#publishDataIsNotCalledAnyMoreThan100TimesPerSecond
```