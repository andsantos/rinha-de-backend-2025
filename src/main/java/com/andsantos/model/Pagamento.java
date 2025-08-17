package com.andsantos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(Pagamento.class)
public record Pagamento(String correlationId, BigDecimal amount, LocalDateTime requestedAt) {

}
