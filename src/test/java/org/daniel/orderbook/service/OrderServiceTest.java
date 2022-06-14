package org.daniel.orderbook.service;

import org.daniel.orderbook.repositories.OrderRepository;
import org.daniel.orderbook.repositories.model.OrderEntity;
import org.daniel.orderbook.rest.model.OrderSide;
import org.daniel.orderbook.rest.model.OrderTicker;
import org.daniel.orderbook.MockData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.daniel.orderbook.MockData.defaultOrderEntity;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void shouldCreateOrder() {
        // Given
        var request = MockData.defaultOrderRequest();

        when(orderRepository.save(any())).thenReturn(OrderEntity.builder()
                                                                .id(1L)
                                                                .build());

        // When
        var id = orderService.createOrder(request);

        // Then
        assert id == 1L;
    }

    @Test
    public void shouldFetchOrder() {
        // Given
        var orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(defaultOrderEntity()));

        // When
        var order = orderService.getOrder(orderId);

        // Then
        assert order.isPresent();
        assert order.get().getOrderId() == orderId;
    }

    @Test
    public void shouldFetchSummary() {
        // Given
        var orderTicker = OrderTicker.GME;
        var orderSide = OrderSide.PURCHASE;
        var expectedMax = BigDecimal.valueOf(10000).stripTrailingZeros();
        var expectedMin = BigDecimal.valueOf(10).stripTrailingZeros();
        // (10 000 * 10 + 10 * 90)/100 = 1009
        var expectedAvg = BigDecimal.valueOf(1009).stripTrailingZeros();
        var expectedTotVolume = 100;

        when(orderRepository.findByOrderTickerAndOrderSideAndCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(eq(orderTicker.name()), eq(orderSide.name()), any(), any()))
                .thenReturn(List.of(defaultOrderEntity(1L, 10L, BigDecimal.valueOf(10000), "SEK"),
                                    defaultOrderEntity(2L, 90L, BigDecimal.valueOf(10), "SEK")));

        // When
        var summaries = orderService.getSummaries(orderTicker, orderSide, LocalDate.now().minusDays(7));

        // Then
        assert summaries.size() == 1;

        var summary = summaries.get(0);
        assert summary.getOrderTicker() == orderTicker;
        assert summary.getOrderSide() == orderSide;
        assert summary.getMaxPrice().equals(expectedMax);
        assert summary.getMinPrice().equals(expectedMin);
        assert summary.getAveragePrice().equals(expectedAvg);
        assert summary.getTotalVolume() == expectedTotVolume;
        assert summary.getCurrency().equals("SEK");
    }

    @Test
    public void shouldGroupAndCalculateSummariesByCurrency() {
        // Given
        var orderTicker = OrderTicker.GME;
        var orderSide = OrderSide.PURCHASE;
        var expectedSekMax = BigDecimal.valueOf(10000).stripTrailingZeros();
        var expectedSekMin = BigDecimal.valueOf(10).stripTrailingZeros();
        // (10 000 * 10 + 10 * 90)/100 = 1009
        var expectedSekAvg = BigDecimal.valueOf(1009).stripTrailingZeros();
        var expectedSekTotVolume = 100;

        var expectedUsdMax = BigDecimal.valueOf(5000).stripTrailingZeros();
        var expectedUsdMin = BigDecimal.valueOf(5).stripTrailingZeros();
        // (5000 * 5 + 20 * 10)/25 = 1004
        var expectedUsdAvg = BigDecimal.valueOf(1004).stripTrailingZeros();
        var expectedUsdTotVolume = 25;

        when(orderRepository.findByOrderTickerAndOrderSideAndCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(eq(orderTicker.name()), eq(orderSide.name()), any(), any()))
                .thenReturn(List.of(defaultOrderEntity(1L, 10L, BigDecimal.valueOf(10000), "SEK"),
                                    defaultOrderEntity(2L, 90L, BigDecimal.valueOf(10), "SEK"),
                                    defaultOrderEntity(50L, 5L, BigDecimal.valueOf(5000), "USD"),
                                    defaultOrderEntity(34L, 20L, BigDecimal.valueOf(5), "USD")));

        // When
        var summaries = orderService.getSummaries(orderTicker, orderSide, LocalDate.now().minusDays(7));

        // Then
        assert summaries.size() == 2;

        var usdSummary = summaries.stream().filter(summary -> summary.getCurrency().equals("USD")).findAny();
        assert usdSummary.isPresent();
        assert usdSummary.get().getOrderTicker() == orderTicker;
        assert usdSummary.get().getOrderSide() == orderSide;
        assert usdSummary.get().getMaxPrice().equals(expectedUsdMax);
        assert usdSummary.get().getMinPrice().equals(expectedUsdMin);
        assert usdSummary.get().getAveragePrice().equals(expectedUsdAvg);
        assert usdSummary.get().getTotalVolume() == expectedUsdTotVolume;
        assert usdSummary.get().getCurrency().equals("USD");

        var sekSummary = summaries.stream().filter(summary -> summary.getCurrency().equals("SEK")).findAny();
        assert sekSummary.isPresent();
        assert sekSummary.get().getOrderTicker() == orderTicker;
        assert sekSummary.get().getOrderSide() == orderSide;
        assert sekSummary.get().getMaxPrice().equals(expectedSekMax);
        assert sekSummary.get().getMinPrice().equals(expectedSekMin);
        assert sekSummary.get().getAveragePrice().equals(expectedSekAvg);
        assert sekSummary.get().getTotalVolume() == expectedSekTotVolume;
        assert sekSummary.get().getCurrency().equals("SEK");
    }
}