package com.redhat.demos.thoughts.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.sql.DataSource;
import java.sql.Connection;

@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    DataSource dataSource;

    @Override
    public HealthCheckResponse call() {
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(2);
            if (isValid) {
                return HealthCheckResponse.up("Database connection");
            } else {
                return HealthCheckResponse.down("Database connection");
            }
        } catch (Exception e) {
            return HealthCheckResponse.down("Database connection");
        }
    }
}
