package com.andsantos.controller;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.nats.client.Connection;

@Controller
public class PagamentoController {

    @Value("${env.NOME_FILA}")
    private String nomeFila;

    private final Connection natsConnection;

    public PagamentoController(Connection natsConnection) {
        this.natsConnection = natsConnection;
    }

    @PostMapping("/payments")
    public ResponseEntity<Void> enviarMensagem(@RequestBody String mensagem) throws Exception {
        String data = ", \"requestAt\" : \"" + Instant.now() + "\" } ";

        natsConnection.publish(nomeFila, mensagem.replace("}", data).getBytes());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/payments-summary")
    public ResponseEntity<String> obterResumo(@RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) throws Exception {
        String json = """
                {
                    "default" : {
                        "totalRequests": 43236,
                        "totalAmount": 415542345.98
                    },
                    "fallback" : {
                        "totalRequests": 423545,
                        "totalAmount": 329347.34
                    }
                }
                        """;

        return ResponseEntity.ok(json);
    }
}
