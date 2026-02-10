package com.redhat.demos.evaluation.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

/**
 * Service for calculating similarity between vector embeddings using cosine similarity.
 */
@ApplicationScoped
public class VectorSimilarityService {

    private static final Logger LOG = Logger.getLogger(VectorSimilarityService.class);

    /**
     * Calculates the cosine similarity between two vectors.
     * Cosine similarity ranges from -1 (opposite) to 1 (identical).
     *
     * @param vectorA first vector
     * @param vectorB second vector
     * @return cosine similarity score between 0.0 and 1.0
     * @throws IllegalArgumentException if vectors have different dimensions
     */
    public double calculateCosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null) {
            throw new IllegalArgumentException("Vectors cannot be null");
        }

        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException(
                String.format("Vector dimensions must match. Got %d and %d",
                    vectorA.length, vectorB.length)
            );
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);

        if (normA == 0.0 || normB == 0.0) {
            LOG.warn("One or both vectors have zero magnitude, returning 0 similarity");
            return 0.0;
        }

        double similarity = dotProduct / (normA * normB);

        // Ensure result is between -1 and 1 due to floating point precision
        similarity = Math.max(-1.0, Math.min(1.0, similarity));

        LOG.debugf("Calculated cosine similarity: %.4f", similarity);
        return similarity;
    }
}
