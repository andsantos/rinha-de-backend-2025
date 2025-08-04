package com.andsantos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Pagamento(String id, BigDecimal amount, LocalDateTime requestedAt) {

}
