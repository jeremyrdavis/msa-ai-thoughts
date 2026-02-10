package com.redhat.demos.evaluation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.demos.evaluation.model.EvaluationVector;
import com.redhat.demos.evaluation.model.ThoughtEvaluation;
import com.redhat.demos.evaluation.model.ThoughtStatus;
import com.redhat.demos.evaluation.model.VectorType;
import com.redhat.demos.evaluation.util.VectorDataParser;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of EvaluationService that orchestrates the evaluation process.
 * Generates embeddings, compares against negative vectors, and persists results.
 */
@ApplicationScoped
public class EvaluationServiceImpl implements EvaluationService {

    private static final Logger LOG = Logger.getLogger(EvaluationServiceImpl.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    EmbeddingService embeddingService;

    @Inject
    VectorSimilarityService vectorSimilarityService;

    @ConfigProperty(name = "evaluation.similarity.threshold", defaultValue = "0.85")
    double similarityThreshold;

    @Override
    @Transactional
    @Counted(value = "evaluation.thoughts.total", description = "Total number of thoughts evaluated")
    @Timed(value = "evaluation.thoughts.duration", description = "Time taken to evaluate thoughts")
    public ThoughtEvaluation evaluateThought(UUID thoughtId, String thoughtContent) {
        String correlationId = UUID.randomUUID().toString();

        LOG.infof("[%s] Starting evaluation for thought %s", correlationId, thoughtId);

        try {
            // Generate embedding for the thought
            float[] thoughtVector = embeddingService.generateEmbedding(thoughtContent);

            // Retrieve all negative vectors from database
            List<EvaluationVector> negativeVectors = EvaluationVector.list("vectorType", VectorType.NEGATIVE);

            LOG.infof("[%s] Retrieved %d negative vectors for comparison", correlationId, negativeVectors.size());

            // Calculate similarity with each negative vector
            double maxSimilarity = 0.0;
            String matchedLabel = null;

            for (EvaluationVector negativeVector : negativeVectors) {
                float[] negativeEmbedding = VectorDataParser.parseVectorData(negativeVector.vectorData);
                double similarity = vectorSimilarityService.calculateCosineSimilarity(thoughtVector, negativeEmbedding);

                LOG.debugf("[%s] Similarity with '%s': %.4f", correlationId, negativeVector.label, similarity);

                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    matchedLabel = negativeVector.label;
                }
            }

            // Determine status based on threshold
            ThoughtStatus status = maxSimilarity > similarityThreshold
                ? ThoughtStatus.REJECTED
                : ThoughtStatus.APPROVED;

            LOG.infof("[%s] Evaluation result: %s (max similarity: %.4f, threshold: %.2f)",
                correlationId, status, maxSimilarity, similarityThreshold);

            // Create and persist evaluation
            ThoughtEvaluation evaluation = createEvaluation(thoughtId, status, maxSimilarity, matchedLabel, correlationId);

            LOG.infof("[%s] Evaluation completed and persisted with id %s", correlationId, evaluation.id);

            return evaluation;

        } catch (Exception e) {
            LOG.errorf(e, "[%s] Failed to evaluate thought %s", correlationId, thoughtId);
            throw new RuntimeException("Evaluation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Creates and persists a ThoughtEvaluation entity.
     */
    private ThoughtEvaluation createEvaluation(UUID thoughtId, ThoughtStatus status,
                                               double similarityScore, String matchedLabel,
                                               String correlationId) {
        ThoughtEvaluation evaluation = new ThoughtEvaluation();
        evaluation.thoughtId = thoughtId;
        evaluation.status = status;
        evaluation.similarityScore = BigDecimal.valueOf(similarityScore);

        // Create metadata JSON
        ObjectNode metadata = MAPPER.createObjectNode();
        metadata.put("evaluationTimestamp", LocalDateTime.now().toString());
        metadata.put("correlationId", correlationId);
        metadata.put("threshold", similarityThreshold);
        metadata.put("modelName", "text-embedding-ada-002");

        if (matchedLabel != null) {
            metadata.put("matchedNegativeLabel", matchedLabel);
        }

        evaluation.metadata = metadata.toString();

        // Persist the evaluation
        evaluation.persist();

        return evaluation;
    }
}
