package com.andsantos.controller;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.andsantos.repositorio.PagamentoRepository;
import com.andsantos.service.NatsPublisher;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Controller
public class PagamentoController {

    private final NatsPublisher publisher;
    private final PagamentoRepository repository;

    @Value("${env.NOME_FILA}")
    private String nomeFila;

    public PagamentoController(NatsPublisher pub, PagamentoRepository repository) {
        this.publisher = pub;
        this.repository = repository;
    }

    @PostMapping("/payments")
    public Mono<ResponseEntity<Void>> enviarMensagem(@RequestBody String mensagem) {
        String data = ", \"requestedAt\" : \"" + Instant.now() + "\" } ";
        String payload = mensagem.replace("}", data);

        return publisher.publish(nomeFila, payload.getBytes(StandardCharsets.UTF_8))
                .thenReturn(ResponseEntity.ok().build());
    }

    @GetMapping(value = "/payments-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> obterResumo(@RequestParam(required = false) ZonedDateTime from,
            @RequestParam(required = false) ZonedDateTime to) {
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
    public Mono<String> status() {
        return Mono.just("OK");
    }
}
