package com.andsantos.config;

import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;

@Configuration
public class NatsConfig {

    @Value("${env.NATS_SERVER}")
    private String natsServerUrl;

    @Bean
    Connection natsConnection() throws IOException, InterruptedException {
        Options options = new Options.Builder()
                .server(natsServerUrl)
                .connectionTimeout(Duration.ofSeconds(2))
                .build();
        return Nats.connect(options);
    }
}
