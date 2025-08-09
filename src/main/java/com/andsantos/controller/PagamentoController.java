package com.andsantos.controller;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    public Mono<ResponseEntity<Void>> enviarMensagem(@RequestBody String mensagem) {
        return Mono.fromRunnable(() -> {
            String data = ", \"requestedAt\" : \"" + Instant.now() + "\" } ";
            natsConnection.publish(nomeFila, mensagem.replace("}", data).getBytes(StandardCharsets.UTF_8));
        })
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @GetMapping("/payments-summary")
    public Mono<ResponseEntity<Resumo>> obterResumo(@RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        return Mono.fromCallable(() -> repository.obterResumo(from, to))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }

    @PostMapping("/admin/purge-payments")
    public Mono<ResponseEntity<String>> zerarBase() {
        return Mono.fromRunnable(repository::purge)
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(ResponseEntity.ok("""
                        { "message": "All payments purged." }
                        """)));
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<String>> status() {
        return Mono.just(ResponseEntity.ok("""
                "status" : "OK"
                """));
    }
}
