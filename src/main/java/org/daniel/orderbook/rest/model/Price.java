package org.daniel.orderbook.rest.model;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Value
@Builder
public class Price {

    @NotNull
    @DecimalMin(value = "0.0")
    BigDecimal amount;

    @NotBlank
    String currency;
}
