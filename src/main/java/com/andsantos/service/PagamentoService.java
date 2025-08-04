package com.andsantos.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.andsantos.model.Pagamento;
import com.andsantos.repositorio.PagamentoRepository;
import com.andsantos.util.impl.HttpClientNative;
import com.andsantos.util.impl.JSONUtil;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;

@Service
public class PagamentoService {

    private final ApplicationContext context;

    @Value("${env.API_PAYMENT_DEFAULT}")
    private String apiProcessorDefault;

    @Value("${env.API_PAYMENT_FALLBACK}")
    private String apiProcessorFallback;

    @Value("${env.NOME_FILA}")
    private String nomeFila;

    private final Connection natsConnection;
    private final JSONUtil jsonUtil;
    private final PagamentoRepository repository;

    public PagamentoService(Connection natsConnection, ApplicationContext context, JSONUtil jsonUtil,
            PagamentoRepository repository) {
        this.natsConnection = natsConnection;
        this.context = context;
        this.jsonUtil = jsonUtil;
        this.repository = repository;
    }

    @PostConstruct
    public void escutar() {
        Dispatcher dispatcher = natsConnection.createDispatcher(this::processar);
        dispatcher.subscribe(nomeFila);
    }

    protected void processar(Message msg) {
        String conteudo = new String(msg.getData(), StandardCharsets.UTF_8);

        PagamentoService proxy = context.getBean(PagamentoService.class);
        proxy.envioPadrao(conteudo);
    }

    @Retryable(retryFor = { Exception.class,
            RuntimeException.class }, maxAttempts = 10)
    protected void envioPadrao(String conteudo) {
        System.out.println("envioPadrao " + LocalDateTime.now());
        HttpClientNative.enviar(apiProcessorDefault, conteudo);

        Pagamento pagamento = jsonUtil.converter(conteudo);
        repository.registrarPagamentoPadrao(pagamento);

        System.out.println("Sucesso do envioPadrao " + LocalDateTime.now());
    }

    @Retryable(retryFor = { Exception.class,
            RuntimeException.class }, maxAttempts = 5) /* , backoff = @Backoff(delay = 1000, multiplier = 2) */
    protected void envioContingencia(String conteudo) {
        System.out.println("envioContingencia " + LocalDateTime.now());
        HttpClientNative.enviar(apiProcessorFallback, conteudo);

        Pagamento pagamento = jsonUtil.converter(conteudo);
        repository.registrarPagamentoContingencia(pagamento);
    }

    @Recover
    protected void fallback(Exception exception, String conteudo) {
        System.out.println("fallback " + LocalDateTime.now());
        PagamentoService proxy = context.getBean(PagamentoService.class);
        proxy.envioContingencia(conteudo);
    }
}
