package com.andsantos.model;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonProperty;

@RegisterReflectionForBinding(Resumo.class)
public class Resumo {
    @JsonProperty(value = "default")
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
