package com.redhat.demos.thoughts.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import java.util.Optional;

@Liveness
@ApplicationScoped
public class LLMHealthCheck implements HealthCheck {

    @ConfigProperty(name = "llm.endpoint.url")
    Optional<String> llmEndpointUrl;

    @ConfigProperty(name = "llm.enabled", defaultValue = "true")
    boolean llmEnabled;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("LLM endpoint");

        if (!llmEnabled) {
            return builder.up()
                    .withData("status", "disabled")
                    .build();
        }

        if (llmEndpointUrl.isPresent() && !llmEndpointUrl.get().isEmpty()) {
            return builder.up()
                    .withData("endpoint", llmEndpointUrl.get())
                    .build();
        } else {
            return builder.down()
                    .withData("reason", "LLM endpoint URL not configured")
                    .build();
        }
    }
}
