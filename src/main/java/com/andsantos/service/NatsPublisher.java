package com.andsantos.service;

import org.springframework.stereotype.Service;

import io.nats.client.Connection;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class NatsPublisher {

    private final Mono<Connection> connection;

    public NatsPublisher(Mono<Connection> conn) {
        this.connection = conn;
    }

    public Mono<Void> publish(String subject, byte[] data) {
        return connection.flatMap(conn -> Mono.fromRunnable(() -> {
            conn.publish(subject, data);
        }).subscribeOn(Schedulers.boundedElastic())).then();
    }
}
