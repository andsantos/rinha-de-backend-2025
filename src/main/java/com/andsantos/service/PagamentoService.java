package com.andsantos.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

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
import jakarta.annotation.PreDestroy;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Service
public class PagamentoService {

    @Value("${env.NOME_FILA}")
    private String nomeFila;

    @Value("${env.API_PAYMENT_DEFAULT}")
    private String apiProcessorDefault;

    @Value("${env.API_PAYMENT_FALLBACK}")
    private String apiProcessorFallback;

    private final Connection natsConnection;
    private final WebClient webClient;
    private final PagamentoRepository repository;
    private final JSONUtil jsonUtil;
    private static final int NR_TENTATIVAS_PADRAO = 7;
    private static final int NR_TENTATIVAS_FALLBACK = 3;

    public PagamentoService(Connection natsConnection, WebClient webClient,
            PagamentoRepository repository, JSONUtil jsonUtil) {
        this.natsConnection = natsConnection;
        this.webClient = webClient;
        this.repository = repository;
        this.jsonUtil = jsonUtil;
    }

    @PostConstruct
    public void escutar() {
        Dispatcher dispatcher = natsConnection.createDispatcher(this::processar);
        dispatcher.subscribe(nomeFila, "grupo.pagamento");
    }

    protected void processar(Message msg) {
        String conteudo = new String(msg.getData(), StandardCharsets.UTF_8);

        envioPadrao(conteudo).doOnError(err -> {
            System.err.println("Erro ao processar mensagem: " + err.getMessage());
        }).subscribe();
    }

    @PreDestroy
    public void fechar() throws InterruptedException {
        if (natsConnection != null && natsConnection.getStatus() != Connection.Status.CLOSED) {
            natsConnection.close();
        }
    }

    protected Mono<Void> envioPadrao(String conteudo) {
        return webClient.post()
                .uri(apiProcessorDefault)
                .header("Content-Type", "application/json")
                .bodyValue(conteudo)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5))
                .retryWhen(
                        Retry.backoff(NR_TENTATIVAS_PADRAO, Duration.ofMillis(200)).maxBackoff(Duration.ofSeconds(2)))
                .then(Mono.fromCallable(() -> {
                    Pagamento pagamento = jsonUtil.converter(conteudo);
                    repository.registrarPagamento(pagamento, "DEFAULT");
                    return null;
                }).subscribeOn(Schedulers.boundedElastic()))
                .onErrorResume(ex -> {
                    return fallback(conteudo);
                }).then();
    }

    protected Mono<Void> fallback(String conteudo) {
        return webClient.post()
                .uri(apiProcessorFallback)
                .header("Content-Type", "application/json")
                .bodyValue(conteudo)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5))
                .retryWhen(
                        Retry.backoff(NR_TENTATIVAS_FALLBACK, Duration.ofMillis(200)).maxBackoff(Duration.ofSeconds(2)))
                .then(Mono.fromCallable(() -> {
                    Pagamento pagamento = jsonUtil.converter(conteudo);
                    repository.registrarPagamento(pagamento, "FALLBACK");
                    return null;
                }).subscribeOn(Schedulers.boundedElastic())).then();
    }
}
