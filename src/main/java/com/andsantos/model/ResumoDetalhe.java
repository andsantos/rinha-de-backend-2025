package com.andsantos.model;

import java.math.BigDecimal;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonProperty;

@RegisterReflectionForBinding(ResumoDetalhe.class)
public class ResumoDetalhe {
    @JsonProperty(value = "totalRequests")
    private Long totalRequests = 0L;

    @JsonProperty(value = "totalAmount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public Long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

}
