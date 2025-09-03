package com.andsantos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Pagamento(String correlationId, BigDecimal amount,
        LocalDateTime requestedAt) {

}
