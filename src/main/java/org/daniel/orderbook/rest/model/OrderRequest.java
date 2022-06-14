package org.daniel.orderbook.rest.model;

import lombok.Builder;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Value
@Builder
public class OrderRequest {

    @NotNull
    OrderTicker orderTicker;

    @NotNull
    OrderSide orderSide;

    @NotNull
    @Min(value = 1L)
    Long volume;

    @Valid
    @NotNull
    Price price;
}
