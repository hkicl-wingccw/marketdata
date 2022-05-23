package com.clsa.marketdata;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.Data;

@Data
public class MarketData {
  public MarketData() {}
  public MarketData(String symbol, double price, LocalDateTime updateTime) {
    this.symbol = symbol;
    this.price = price;
    this.updateTime = updateTime;
  }

  @JsonProperty("symbol")
  private String symbol;
  @JsonProperty("price")
  private double price;

  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonProperty("updateTime")
  private LocalDateTime updateTime;
}
