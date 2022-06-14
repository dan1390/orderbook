package org.daniel.orderbook;

import org.daniel.orderbook.repositories.model.OrderEntity;
import org.daniel.orderbook.rest.model.OrderRequest;
import org.daniel.orderbook.rest.model.OrderResponse;
import org.daniel.orderbook.rest.model.OrderSide;
import org.daniel.orderbook.rest.model.OrderTicker;
import org.daniel.orderbook.rest.model.Price;

import java.math.BigDecimal;
import java.time.Instant;

public class MockData {

    public static OrderRequest defaultOrderRequest() {
        return defaultOrderRequest("SEK");
    }

    public static OrderRequest defaultOrderRequest(String currency) {
        return OrderRequest.builder()
                           .orderTicker(OrderTicker.TSLA)
                           .orderSide(OrderSide.SALE)
                           .volume(500L)
                           .price(Price.builder()
                                       .amount(BigDecimal.TEN)
                                       .currency(currency)
                                       .build())
                           .build();
    }

    public static OrderResponse defaultOrderResponse() {
        return OrderResponse.builder()
                            .orderId(1L)
                            .orderTicker(OrderTicker.TSLA)
                            .orderSide(OrderSide.SALE)
                            .volume(500L)
                            .price(BigDecimal.TEN)
                            .currency("SEK")
                            .build();
    }


    public static OrderEntity defaultOrderEntity() {
        return defaultOrderEntity(1L, 500L, BigDecimal.TEN, "SEK");
    }

    public static OrderEntity defaultOrderEntity(Long id,
                                                 Long volume,
                                                 BigDecimal price,
                                                 String currency) {
        return OrderEntity.builder()
                          .id(id)
                          .orderTicker(OrderTicker.TSLA.name())
                          .orderSide(OrderSide.SALE.name())
                          .volume(volume)
                          .price(price)
                          .currency(currency)
                          .createdAt(Instant.now())
                          .build();
    }
}
