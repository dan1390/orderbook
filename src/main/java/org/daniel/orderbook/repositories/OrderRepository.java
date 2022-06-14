package org.daniel.orderbook.repositories;

import org.daniel.orderbook.repositories.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByOrderTickerAndOrderSideAndCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(String orderTicker, String orderSide, Instant from, Instant to);
}
