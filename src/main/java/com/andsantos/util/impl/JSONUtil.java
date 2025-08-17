package com.andsantos.util.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.andsantos.model.Pagamento;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

@Component
public class JSONUtil {
    protected final Log log = LogFactory.getLog(getClass());
    private final ObjectMapper objectMapper;

    public JSONUtil() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new JavaTimeModule());

    }

    public Pagamento converter(String json) {
        try {
            return objectMapper.readValue(json, Pagamento.class);
        } catch (Exception e) {
            log.error("Erro ao converter JSON para record: ", e);
            throw new RuntimeException("Erro ao converter JSON para record: " + e.getMessage(), e);
        }
    }
}
