package com.andsantos.service;

import java.time.Duration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.andsantos.model.Pagamento;
import com.andsantos.repositorio.PagamentoRepository;
import com.andsantos.util.impl.JSONUtil;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Service
public class PagamentoService {

    protected final Log log = LogFactory.getLog(getClass());
    private final Mono<Connection> natsConnection;
    private final WebClient webClient;
    private final PagamentoRepository repository;
    private final JSONUtil jsonUtil;
    private static final int NR_TENTATIVAS_PADRAO = 7;
    private static final int NR_TENTATIVAS_FALLBACK = 3;

    @Value("${env.NOME_FILA}")
    private String nomeFila;

    @Value("${env.API_PAYMENT_DEFAULT}")
    private String apiProcessorDefault;

    @Value("${env.API_PAYMENT_FALLBACK}")
    private String apiProcessorFallback;

    public PagamentoService(Mono<Connection> natsConnection, WebClient webClient,
            PagamentoRepository repository, JSONUtil jsonUtil) {
        this.natsConnection = natsConnection;
        this.webClient = webClient;
        this.repository = repository;
        this.jsonUtil = jsonUtil;
    }

    @PostConstruct
    public void escutar() {
        natsConnection.subscribe(conn -> {
            Dispatcher dispatcher = conn.createDispatcher(this::processar);
            dispatcher.subscribe(nomeFila, "grupo.pagamento");
        }, err -> {
            log.error("Erro ao conectar ao NATS: " + err.getMessage(), err);
        });
    }

    protected void processar(Message msg) {
        envioPadrao(msg.getData()).doOnError(err -> {
            log.error("Erro ao processar mensagem: ", err);
        }).subscribe();
    }

    protected Mono<Void> envioPadrao(byte[] bytes) {
        Pagamento pagamento = jsonUtil.converter(bytes);
        return webClient.post()
                .uri(apiProcessorDefault)
                .header("Content-Type", "application/json")
                .bodyValue(bytes)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5))
                .retryWhen(
                        Retry.backoff(NR_TENTATIVAS_PADRAO, Duration.ofMillis(200)).maxBackoff(Duration.ofSeconds(2)))
                .then(Mono.fromCallable(() -> {
                    repository.registrarPagamento(pagamento, "DEFAULT");
                    return null;
                }).subscribeOn(Schedulers.boundedElastic()))
                .onErrorResume(ex -> {
                    return fallback(bytes, pagamento);
                }).then();
    }

    protected Mono<Void> fallback(byte[] bytes, Pagamento pagamento) {
        return webClient.post()
                .uri(apiProcessorFallback)
                .header("Content-Type", "application/json")
                .bodyValue(bytes)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5))
                .retryWhen(
                        Retry.backoff(NR_TENTATIVAS_FALLBACK, Duration.ofMillis(200)).maxBackoff(Duration.ofSeconds(2)))
                .then(Mono.fromCallable(() -> {
                    repository.registrarPagamento(pagamento, "FALLBACK");
                    return null;
                }).subscribeOn(Schedulers.boundedElastic())).then();
    }
}
