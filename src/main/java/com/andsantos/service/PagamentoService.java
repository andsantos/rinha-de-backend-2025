package com.andsantos.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;

@Service
public class PagamentoService {

    @Value("${env.NOME_FILA}")
    private String nomeFila;

    private final Connection natsConnection;
    private final ProcessorDefaultService worker;

    public PagamentoService(Connection natsConnection, ProcessorDefaultService worker) {
        this.natsConnection = natsConnection;
        this.worker = worker;
    }

    @PostConstruct
    public void escutar() {
        Dispatcher dispatcher = natsConnection.createDispatcher(this::processar);
        dispatcher.subscribe(nomeFila, "grupo.pagamento");
    }

    protected void processar(Message msg) {
        String conteudo = new String(msg.getData(), StandardCharsets.UTF_8);

        worker.processar(conteudo);
    }

}
