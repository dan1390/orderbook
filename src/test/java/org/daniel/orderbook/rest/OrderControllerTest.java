package org.daniel.orderbook.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.daniel.orderbook.rest.model.OrderSide;
import org.daniel.orderbook.rest.model.OrderTicker;
import org.daniel.orderbook.service.OrderService;
import org.daniel.orderbook.MockData;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OrderService orderService;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldCreateOrder() throws Exception {
        // Given
        var request = MockData.defaultOrderRequest();
        when(orderService.createOrder(any())).thenReturn(1L);

        // When
        mockMvc.perform(post("/v1/order/create")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnBadRequestForInvalidCurrency() throws Exception {
        // Given
        var request = MockData.defaultOrderRequest("FOO");
        when(orderService.createOrder(any())).thenThrow(new IllegalArgumentException("Nope!"));

        // When
        mockMvc.perform(post("/v1/order/create")
                .contentType("application/json")
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnOrder() throws Exception {
        // Given
        when(orderService.getOrder(1L)).thenReturn(Optional.of(MockData.defaultOrderResponse()));

        // When
        mockMvc.perform(get("/v1/order/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.orderId", Matchers.is(1)));
    }

    @Test
    public void shouldReturnNotFoundWhenNoMatchingId() throws Exception {
        // Given
        when(orderService.getOrder(1L)).thenReturn(Optional.empty());

        // When
        mockMvc.perform(get("/v1/order/1"))
               .andExpect(status().isNotFound());
    }
    @Test
    public void shouldReturnBadRequestWhenInvalidId() throws Exception {
        // When
        mockMvc.perform(get("/v1/order/foo"))
               .andExpect(status().isBadRequest());

        // Then
        verifyNoInteractions(orderService);
    }

    @Test
    public void shouldReturnOrderSummary() throws Exception {
        // Given
        var orderTicker = OrderTicker.SAVE;
        var orderSide = OrderSide.PURCHASE;
        var date = "2022-01-01";

        when(orderService.getSummaries(orderTicker, orderSide, LocalDate.parse(date)))
                .thenReturn(Collections.emptyList());

        // When
        mockMvc.perform(get("/v1/order/summary").param("orderTicker", orderTicker.name())
                                                           .param("orderSide", orderSide.name())
                                                           .param("date", date))
               .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnBadRequestWhenMissingFilterParam() throws Exception {
        // Given
        var orderTicker = OrderTicker.SAVE;
        var date = "2022-01-01";

        // When
        mockMvc.perform(get("/v1/order/summary").param("orderTicker", orderTicker.name())
                                                           .param("date", date))
               .andExpect(status().isBadRequest());

        // Then
        verifyNoInteractions(orderService);
    }

    @Test
    public void shouldReturnBadRequestWhenInvalidDate() throws Exception {
        // Given
        var orderTicker = OrderTicker.SAVE;
        var orderSide = OrderSide.PURCHASE;
        var date = "2022-01-32";

        // When
        mockMvc.perform(get("/v1/order/summary").param("orderTicker", orderTicker.name())
                                                           .param("orderSide", orderSide.name())
                                                           .param("date", date))
               .andExpect(status().isBadRequest());

        // Then
        verifyNoInteractions(orderService);
    }
}