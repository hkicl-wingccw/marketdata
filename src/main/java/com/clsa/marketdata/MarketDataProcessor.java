package com.clsa.marketdata;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.reactivex.rxjava3.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MarketDataProcessor {
  public static PublishSubject<MarketData> pubSub = PublishSubject.create();
  
  public MarketDataProcessor(@Value("${window:1}") long window, @Value("${throttle:100}") long throttle) {
    pubSub
      .window(window, TimeUnit.SECONDS)
      .subscribe(
        marketDataObs -> marketDataObs
          .distinct(data -> data.getSymbol())
          .take(throttle)
          .subscribe(
            marketData -> this.publishAggregatedMarketData(marketData),
            error -> log.error("publish error: {}", error.getMessage())),
        error -> log.error("observe error: {}", error.getMessage())
    );
  }

  // Receive incoming market data
  public void onMessage(MarketData data) {
    pubSub.onNext(data);
  }

  // Publish aggregted and throttled market data
  public void publishAggregatedMarketData(MarketData data) {
    // Do Nothing, assume implemented.
  }

}
