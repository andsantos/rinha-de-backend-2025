package com.andsantos.model;

import java.math.BigDecimal;

public class ResumoDetalhe {
    private Long totalRequests = 0L;
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
