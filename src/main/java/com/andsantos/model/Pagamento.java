package com.andsantos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@RegisterReflectionForBinding(Pagamento.class)
public record Pagamento(@JsonProperty("correlationId") String correlationId, @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("requestedAt") LocalDateTime requestedAt) {

    @JsonCreator
    public Pagamento {
    }
}
