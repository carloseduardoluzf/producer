package org.acme;

import java.math.BigDecimal;

public record OrderRequest(String product, int qty, BigDecimal price) {
}
