package org.daniel.orderbook.rest.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum OrderTicker {
    GME("GameStop"),
    TSLA("Tesla"),
    SAVE("Nordnet");

    private final String name;
}
