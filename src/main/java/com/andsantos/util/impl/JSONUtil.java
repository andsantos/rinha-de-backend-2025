package com.andsantos.util.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.andsantos.model.Pagamento;
import com.fasterxml.jackson.databind.JsonNode;
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

    public Pagamento converter(byte[] json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            String correlationId = node.path("correlationId").asText();
            BigDecimal amount = new BigDecimal(node.path("amount").asText());
            LocalDateTime requestedAt = LocalDateTime.parse(node.path("requestedAt").asText(),
                    DateTimeFormatter.ISO_DATE_TIME);
            return new Pagamento(correlationId, amount, requestedAt);
        } catch (Exception e) {
            log.error("Erro ao converter JSON para record: ", e);
            throw new RuntimeException("Erro ao converter JSON para record: " + e.getMessage(), e);
        }
    }
}
