package com.redhat.demos.thoughts.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class EvaluationMetrics {

    @Inject
    MeterRegistry registry;

    private Counter approvedCounter;
    private Counter rejectedCounter;
    private Counter llmSuccessCounter;
    private Counter llmFailureCounter;
    private Timer evaluationTimer;
    private final AtomicReference<Double> averageSimilarityScore = new AtomicReference<>(0.0);

    public void init() {
        if (approvedCounter == null) {
            approvedCounter = Counter.builder("evaluation.approved.total")
                    .description("Total number of approved evaluations")
                    .register(registry);

            rejectedCounter = Counter.builder("evaluation.rejected.total")
                    .description("Total number of rejected evaluations")
                    .register(registry);

            llmSuccessCounter = Counter.builder("evaluation.llm.success.total")
                    .description("Total number of successful LLM calls")
                    .register(registry);

            llmFailureCounter = Counter.builder("evaluation.llm.failure.total")
                    .description("Total number of failed LLM calls")
                    .register(registry);

            evaluationTimer = Timer.builder("evaluation.processing.time")
                    .description("Time taken to process evaluations")
                    .register(registry);

            registry.gauge("evaluation.similarity.average", averageSimilarityScore, AtomicReference::get);
        }
    }

    public void recordApproved() {
        init();
        approvedCounter.increment();
    }

    public void recordRejected() {
        init();
        rejectedCounter.increment();
    }

    public void recordLlmSuccess() {
        init();
        llmSuccessCounter.increment();
    }

    public void recordLlmFailure() {
        init();
        llmFailureCounter.increment();
    }

    public Timer getEvaluationTimer() {
        init();
        return evaluationTimer;
    }

    public void updateAverageSimilarityScore(BigDecimal score) {
        init();
        if (score != null) {
            averageSimilarityScore.set(score.doubleValue());
        }
    }
}
