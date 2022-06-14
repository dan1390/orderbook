package org.daniel.orderbook.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.daniel.orderbook.rest.model.OrderRequest;
import org.daniel.orderbook.rest.model.OrderResponse;
import org.daniel.orderbook.rest.model.OrderSide;
import org.daniel.orderbook.rest.model.OrderSummaryResponse;
import org.daniel.orderbook.rest.model.OrderTicker;
import org.daniel.orderbook.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.v;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/v1/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping(path = "create")
    public ResponseEntity<Long> createOrder(@RequestBody @Validated OrderRequest request) {
        log.info("Received request to create order", v("request", request));

        Long orderId = orderService.createOrder(request);

        return ResponseEntity.ok(orderId);
    }

    @GetMapping(path = "{orderId}")
    public ResponseEntity<OrderResponse> fetchOrder(@PathVariable("orderId") Long orderId) {
        log.info("Received request to fetch order", v("orderId", orderId));

        return orderService.getOrder(orderId)
                            .map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/summary")
    public ResponseEntity<List<OrderSummaryResponse>> fetchSummary(@RequestParam OrderTicker orderTicker,
                                                                   @RequestParam OrderSide orderSide,
                                                                   @RequestParam String date) {
        log.info("Received request to fetch purchase summary",
                 v("orderTicker", orderTicker),
                 v("orderSide", orderSide),
                 v("date", date));

        try {
            return ResponseEntity.ok(orderService.getSummaries(orderTicker, orderSide, LocalDate.parse(date)));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date!");
        }

    }

    @ExceptionHandler
    public ResponseEntity<String> handleException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getLocalizedMessage());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleException(MethodArgumentNotValidException e) {
        var errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return ResponseEntity.badRequest().body(errors);
    }
}
