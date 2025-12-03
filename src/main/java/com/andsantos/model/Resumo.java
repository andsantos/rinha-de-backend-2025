package com.andsantos.model;

public class Resumo {
    private ResumoDetalhe padrao = new ResumoDetalhe();
    private ResumoDetalhe fallback = new ResumoDetalhe();

    public ResumoDetalhe getPadrao() {
        return padrao;
    }

    public void setPadrao(ResumoDetalhe padrao) {
        this.padrao = padrao;
    }

    public ResumoDetalhe getFallback() {
        return fallback;
    }

    public void setFallback(ResumoDetalhe fallback) {
        this.fallback = fallback;
    }

}
