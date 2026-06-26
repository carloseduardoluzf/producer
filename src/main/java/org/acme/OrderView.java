package org.acme;

import java.math.BigDecimal;

public record OrderView(long id, String product, int qty, BigDecimal price) {
}
