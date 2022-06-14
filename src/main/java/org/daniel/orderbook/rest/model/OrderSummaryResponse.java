package org.daniel.orderbook.rest.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class OrderSummaryResponse {

    OrderTicker orderTicker;
    OrderSide orderSide;
    BigDecimal maxPrice;
    BigDecimal minPrice;
    BigDecimal averagePrice;
    Long totalVolume;
    String currency;
    LocalDate date;
}
