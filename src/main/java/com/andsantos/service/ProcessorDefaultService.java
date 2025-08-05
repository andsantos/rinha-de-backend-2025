package com.andsantos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.andsantos.model.Pagamento;
import com.andsantos.repositorio.PagamentoRepository;
import com.andsantos.util.impl.HttpClientNative;
import com.andsantos.util.impl.JSONUtil;

@Component
public class ProcessorDefaultService {

    @Value("${env.API_PAYMENT_DEFAULT}")
    private String apiProcessorDefault;

    private final JSONUtil jsonUtil;
    private final PagamentoRepository repository;
    private final ProcessorFallbackService fallback;

    public ProcessorDefaultService(JSONUtil jsonUtil,
            PagamentoRepository repository, ProcessorFallbackService worker) {
        this.jsonUtil = jsonUtil;
        this.repository = repository;
        this.fallback = worker;
    }

    @Retryable(retryFor = { Exception.class,
            RuntimeException.class }, maxAttempts = 10)
    protected void processar(String conteudo) {
        try {
            HttpClientNative.enviar(apiProcessorDefault, conteudo);
            Pagamento pagamento = jsonUtil.converter(conteudo);
            repository.registrarPagamento(pagamento, "DEFAULT");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gravar no padrão : ", e);
        }
    }

    @Recover
    protected void fallback(Exception exception, String conteudo) {
        fallback.processar(conteudo);
    }
}
