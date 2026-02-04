package com.redhat.demos.thoughts.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Readiness
@ApplicationScoped
public class KafkaHealthCheck implements HealthCheck {

    @ConfigProperty(name = "kafka.bootstrap.servers", defaultValue = "localhost:9092")
    String kafkaBootstrapServers;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Kafka broker");
        builder.up();
        builder.withData("bootstrap.servers", kafkaBootstrapServers);
        return builder.build();
    }
}
