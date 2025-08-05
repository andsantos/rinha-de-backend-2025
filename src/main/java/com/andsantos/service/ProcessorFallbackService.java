package com.andsantos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.andsantos.model.Pagamento;
import com.andsantos.repositorio.PagamentoRepository;
import com.andsantos.util.impl.HttpClientNative;
import com.andsantos.util.impl.JSONUtil;

@Component
public class ProcessorFallbackService {
    @Value("${env.API_PAYMENT_FALLBACK}")
    private String apiProcessorFallback;

    private final JSONUtil jsonUtil;
    private final PagamentoRepository repository;

    public ProcessorFallbackService(JSONUtil jsonUtil,
            PagamentoRepository repository) {
        this.jsonUtil = jsonUtil;
        this.repository = repository;
    }

    @Retryable(retryFor = { Exception.class,
            RuntimeException.class }, maxAttempts = 5)
    protected void processar(String conteudo) {

        try {
            HttpClientNative.enviar(apiProcessorFallback, conteudo);
            Pagamento pagamento = jsonUtil.converter(conteudo);
            repository.registrarPagamento(pagamento, "FALLBACK");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gravar na contigência : ", e);
        }
    }

}
