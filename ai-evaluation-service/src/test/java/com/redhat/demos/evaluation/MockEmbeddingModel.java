package com.redhat.demos.evaluation;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Mock embedding model for testing.
 * Returns deterministic embeddings based on text content patterns.
 */
@Mock
@ApplicationScoped
public class MockEmbeddingModel implements EmbeddingModel {

    @Override
    public Response<Embedding> embed(TextSegment textSegment) {
        String text = textSegment.text().toLowerCase();

        // Return different embeddings based on content patterns
        float[] vector;

        if (text.contains("hate") || text.contains("terrible") || text.contains("awful")) {
            // Return a vector similar to negative vectors (high negative values)
            vector = createNegativeVector();
        } else if (text.contains("grateful") || text.contains("wonderful") || text.contains("opportunity")) {
            // Return a vector similar to positive vectors
            vector = createPositiveVector();
        } else {
            // Return a neutral vector
            vector = createNeutralVector();
        }

        Embedding embedding = new Embedding(vector);
        return Response.from(embedding);
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> list) {
        throw new UnsupportedOperationException("embedAll not implemented in mock");
    }

    /**
     * Creates a vector with negative-like patterns (matching seeded negative vectors).
     */
    private float[] createNegativeVector() {
        float[] vector = new float[384]; // Standard embedding dimension
        for (int i = 0; i < vector.length; i++) {
            vector[i] = -0.09f + (float) (Math.random() * 0.02f); // Values around -0.09 to -0.07
        }
        return vector;
    }

    /**
     * Creates a vector with positive-like patterns.
     */
    private float[] createPositiveVector() {
        float[] vector = new float[384];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (Math.random() * 0.2f - 0.1f); // Values between -0.1 and 0.1
        }
        return vector;
    }

    /**
     * Creates a neutral vector.
     */
    private float[] createNeutralVector() {
        float[] vector = new float[384];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (Math.random() * 0.1f - 0.05f); // Small random values
        }
        return vector;
    }
}
