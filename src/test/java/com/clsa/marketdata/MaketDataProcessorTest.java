package com.clsa.marketdata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class MaketDataProcessorTest {
  List<String> symbols = new ArrayList<>();

  @BeforeEach
  void initialize(){
    for(int i = 0; i < 500; i ++) {
      symbols.add(generateRandomSymbol(3));
    }
  }

  @Test
  public void publishDataIsNotCalledAnyMoreThan100TimesPerSecond() throws InterruptedException {
    int expectedResult = 100;
    List<MarketData> publishedMarketDatas = new ArrayList<>();
    MarketDataProcessor marketDataProcessor = new MarketDataProcessor(1, 100) {
      @Override
      public void publishAggregatedMarketData(MarketData data) {
        super.publishAggregatedMarketData(data);
        publishedMarketDatas.add(data);
      }
    };
    generateData(marketDataProcessor, symbols);
    log.info("symbols length: {}", symbols.size());
    log.info("marketData published list: {}", publishedMarketDatas.size());
    assertEquals(expectedResult, publishedMarketDatas.size());
  }

  @ParameterizedTest
  @CsvSource({"200,200", "300,300", "400,400"})
  public void publishDataIsNotCalledAnyMoreThanGivenTimesPerSecond(long throttle, int expectedResult) throws InterruptedException {
    List<MarketData> publishedMarketDatas = new ArrayList<>();
    MarketDataProcessor marketDataProcessor = new MarketDataProcessor(1, throttle) {
      @Override
      public void publishAggregatedMarketData(MarketData data) {
        super.publishAggregatedMarketData(data);
        publishedMarketDatas.add(data);
      }
    };
    generateData(marketDataProcessor, symbols);
    log.info("symbols length: {}", symbols.size());
    log.info("marketData published list: {}", publishedMarketDatas.size());
    assertEquals(expectedResult, publishedMarketDatas.size());
  }

  @ParameterizedTest(name = "Only {1} records would be published from duplicatedSymbols: {0}")
  @MethodSource("generateDuplicateData")
  public void publishDataWithoutAnyDuplicateSymbolPerSecond(List<String> duplicatedSymbols, int expectedResult) throws InterruptedException {
    List<String> publishedSymbols = new ArrayList<>();
    MarketDataProcessor marketDataProcessor = new MarketDataProcessor(1, 100) {
      @Override
      public void publishAggregatedMarketData(MarketData data) {
        super.publishAggregatedMarketData(data);
        publishedSymbols.add(data.getSymbol());
      }
    };
    generateData(marketDataProcessor, duplicatedSymbols);
    log.info("publishedSymbols: {}", publishedSymbols);
    assertEquals(expectedResult, publishedSymbols.size());
  }

  // published data would be doubled if sleep time is 1 second.
  @ParameterizedTest(name = "Only {3} records would be published from duplicatedSymbols - {0} per second if sleep time is {1}")
  @MethodSource("generateDuplicateDataWithSleepTime")
  public void ensureEachSymbolWillNotBePublishedMoreThanOnePerSecond(List<String> duplicatedSymbols, long sleepTime, int noOfLoop, int expectedResult) throws InterruptedException {
    List<String> publishedSymbols = new ArrayList<>();
    MarketDataProcessor marketDataProcessor = new MarketDataProcessor(1, 100) {
      @Override
      public void publishAggregatedMarketData(MarketData data) {
        super.publishAggregatedMarketData(data);
        publishedSymbols.add(data.getSymbol());
      }
    };
    for(int i = 0; i < noOfLoop; i++) {
      generateData(marketDataProcessor, duplicatedSymbols);
      // sleep 1 second to ensure each symbol will not be published more than one times per second.
      Thread.sleep(sleepTime);
    }
    log.info("publishedSymbols: {}", publishedSymbols);
    assertEquals(expectedResult, publishedSymbols.size());
  }

  static Stream<Arguments> generateDuplicateData() {
    return Stream.of(
      Arguments.of(Arrays.asList("AAA", "AAA" , "BBB", "BBB", "CCC", "CCC"), 3),
      Arguments.of(Arrays.asList("AAA", "AAA" , "BBB", "BBB", "CCC", "CCC", "DDD", "DDD"), 4),
      Arguments.of(Arrays.asList("AAA", "AAA" , "BBB", "BBB", "CCC", "CCC", "DDD", "DDD", "DDD", "EEE", "EEE"), 5)
    );
  }

  static Stream<Arguments> generateDuplicateDataWithSleepTime() {
    return Stream.of(
      Arguments.of(Arrays.asList("AAA", "AAA" , "BBB", "BBB", "CCC", "CCC"), 0, 2, 3),
      Arguments.of(Arrays.asList("AAA", "AAA" , "BBB", "BBB", "CCC", "CCC", "DDD", "DDD"), 1000, 2, 8),
      Arguments.of(Arrays.asList("AAA", "AAA" , "BBB", "BBB", "CCC", "CCC", "DDD", "DDD", "DDD", "EEE", "EEE"), 1000, 2, 10)
    );
  }

  private void generateData(MarketDataProcessor marketDataProcessor, List<String> symbols) throws InterruptedException {
    for(String symbol: symbols) {
      marketDataProcessor.onMessage(new MarketData(symbol, 1.00, LocalDateTime.now()));
    }
  }

  private static String generateRandomSymbol(int len) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(chars.charAt(rnd.nextInt(chars.length())));
		return sb.toString();
  }
}
