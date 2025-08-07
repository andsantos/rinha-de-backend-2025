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

import com.andsantos.model.Resumo;
import com.andsantos.repositorio.PagamentoRepository;

import io.nats.client.Connection;

@Controller
public class PagamentoController {

    @Value("${env.NOME_FILA}")
    private String nomeFila;

    private final Connection natsConnection;
    private final PagamentoRepository repository;

    public PagamentoController(Connection natsConnection, PagamentoRepository repository) {
        this.natsConnection = natsConnection;
        this.repository = repository;
    }

    @PostMapping("/payments")
    public ResponseEntity<Void> enviarMensagem(@RequestBody String mensagem) throws Exception {
        String data = ", \"requestedAt\" : \"" + Instant.now() + "\" } ";

        natsConnection.publish(nomeFila, mensagem.replace("}", data).getBytes());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/payments-summary")
    public ResponseEntity<Resumo> obterResumo(@RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) throws Exception {
        Resumo resumo = repository.obterResumo(from, to);
        return ResponseEntity.ok(resumo);
    }

    @PostMapping("/admin/purge-payments")
    public ResponseEntity<String> zerarBase() throws Exception {

        repository.purge();

        return ResponseEntity.ok("""
                { "message": "All payments purged." }
                """);
    }
}
