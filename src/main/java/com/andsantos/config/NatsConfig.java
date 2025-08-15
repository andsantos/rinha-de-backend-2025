package com.andsantos.config;

import java.time.Duration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Configuration
public class NatsConfig {
    protected final Log log = LogFactory.getLog(getClass());

    @Bean
    Mono<Connection> natsConnection(
            @Value("${env.NATS_SERVER}") String natsUrl) {

        return Mono.fromCallable(() -> {
            Options options = new Options.Builder()
                    .server(natsUrl)
                    .maxReconnects(-1)
                    .reconnectWait(Duration.ofSeconds(2))
                    .connectionName("payment-api")
                    .build();

            Connection conn = Nats.connect(options);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    conn.close();
                } catch (Exception e) {
                    log.error("Erro ao fechar conexão: ", e);
                }
            }));

            return conn;
        })
                .subscribeOn(Schedulers.boundedElastic())
                .retryWhen(
                        Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(3))
                                .maxBackoff(Duration.ofSeconds(30)))
                .cache();
    }
}
