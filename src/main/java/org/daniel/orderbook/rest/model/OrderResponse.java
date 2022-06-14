package org.daniel.orderbook.rest.model;

import lombok.Builder;
import lombok.Value;
import org.daniel.orderbook.repositories.model.OrderEntity;

import java.math.BigDecimal;

@Value
@Builder
public class OrderResponse {

    Long orderId;
    OrderTicker orderTicker;
    OrderSide orderSide;
    Long volume;
    BigDecimal price;
    String currency;

    public static OrderResponse from(OrderEntity entity) {
        return OrderResponse.builder()
                            .orderId(entity.getId())
                            .orderTicker(OrderTicker.valueOf(entity.getOrderTicker()))
                            .orderSide(OrderSide.valueOf(entity.getOrderSide()))
                            .volume(entity.getVolume())
                            .price(entity.getPrice())
                            .currency(entity.getCurrency())
                            .build();
    }
}
