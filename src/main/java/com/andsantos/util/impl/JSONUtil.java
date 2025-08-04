package com.andsantos.util.impl;

import org.springframework.stereotype.Component;

import com.andsantos.model.Pagamento;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JSONUtil {

    private final ObjectMapper objectMapper;

    public JSONUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Pagamento converter(String json) {
        try {
            return objectMapper.readValue(json, Pagamento.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter JSON para record", e);
        }
    }
}
