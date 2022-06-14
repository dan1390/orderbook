package org.daniel.orderbook.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.daniel.orderbook.repositories.OrderRepository;
import org.daniel.orderbook.repositories.model.OrderEntity;
import org.daniel.orderbook.rest.model.OrderRequest;
import org.daniel.orderbook.rest.model.OrderResponse;
import org.daniel.orderbook.rest.model.OrderSide;
import org.daniel.orderbook.rest.model.OrderSummaryResponse;
import org.daniel.orderbook.rest.model.OrderTicker;
import org.daniel.orderbook.rest.model.Price;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;

import javax.money.UnknownCurrencyException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Long createOrder(OrderRequest request) {
        return orderRepository.save(convertToEntity(request))
                              .getId();
    }

    public Optional<OrderResponse> getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                              .map(OrderResponse::from);
    }

    // TODO: Implement currency conversion and return unified summary in a single currency
    public List<OrderSummaryResponse> getSummaries(OrderTicker orderTicker,
                                                   OrderSide orderSide,
                                                   LocalDate date) {
        var allMatchingOrders = orderRepository.findByOrderTickerAndOrderSideAndCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(orderTicker.name(),
                                                                                         orderSide.name(),
                                                                                         date.atTime(LocalTime.MIN).toInstant(ZoneOffset.UTC),
                                                                                         date.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC));

        return allMatchingOrders.stream()
                                .collect(Collectors.groupingBy(OrderEntity::getCurrency))
                                .entrySet()
                                .stream()
                                .map(entry -> calculateOrderSummary(orderTicker, orderSide, entry.getValue(), entry.getKey(), date))
                                .collect(Collectors.toList());
    }

    private static OrderSummaryResponse calculateOrderSummary(OrderTicker orderTicker,
                                                              OrderSide orderSide,
                                                              List<OrderEntity> orders,
                                                              String currency,
                                                              LocalDate date) {
        var prices = orders.stream()
                .map(order -> Money.of(order.getPrice(), order.getCurrency()))
                .collect(Collectors.toList());

        var maxPrice = prices.stream().max(Money::compareTo).map(Money::getNumberStripped);
        var minPrice = prices.stream().min(Money::compareTo).map(Money::getNumberStripped);

        var totalVolume = orders.stream().map(OrderEntity::getVolume).reduce(Long::sum);

        var totalSum = orders.stream().map(order -> Money.of(order.getPrice(), order.getCurrency()).multiply(order.getVolume()))
                .reduce(Money::add);

        var avgPrice = totalVolume.flatMap(totVol -> totalSum.map(totSum -> totSum.divide(totVol))).map(Money::getNumberStripped);

        if (avgPrice.isEmpty()) {
            log.error("Failed to calculate average price! Total sum: {} Total volume: {}", totalSum.orElse(null), totalVolume.orElse(null));
        }

        return OrderSummaryResponse.builder()
                                   .orderTicker(orderTicker)
                                   .orderSide(orderSide)
                                   .maxPrice(maxPrice.orElse(null))
                                   .minPrice(minPrice.orElse(null))
                                   .averagePrice(avgPrice.orElse(null))
                                   .totalVolume(totalVolume.orElse(null))
                                   .currency(currency)
                                   .date(date)
                                   .build();
    }

    private static Money convertToMonetaryAmount(Price price) {
        try {
            return Money.of(price.getAmount(), price.getCurrency());
        } catch (UnknownCurrencyException e) {
            log.error("Failed to convert to monetary amount!", e);
            throw new IllegalArgumentException("Invalid currency: " + price.getCurrency());
        }
    }

    private static OrderEntity convertToEntity(OrderRequest request) {
        var monetaryAmount = convertToMonetaryAmount(request.getPrice());

        return OrderEntity.builder()
                          .orderTicker(request.getOrderTicker().name())
                          .orderSide(request.getOrderSide().name())
                          .volume(request.getVolume())
                          .price(monetaryAmount.getNumberStripped())
                          .currency(monetaryAmount.getCurrency().getCurrencyCode())
                          .createdAt(Instant.now())
                          .build();
    }
}
