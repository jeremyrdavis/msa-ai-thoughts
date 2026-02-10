package com.redhat.demos.evaluation.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

/**
 * Utility class for parsing vector data from JSON format.
 */
public class VectorDataParser {

    private static final Logger LOG = Logger.getLogger(VectorDataParser.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Parses a JSON string containing vector embedding data into a float array.
     * Expected format: {"embedding": [0.1, 0.2, 0.3, ...]}
     *
     * @param jsonData JSON string containing the embedding array
     * @return float array of embedding values
     * @throws RuntimeException if parsing fails
     */
    public static float[] parseVectorData(String jsonData) {
        try {
            JsonNode root = MAPPER.readTree(jsonData);
            JsonNode embeddingNode = root.get("embedding");

            if (embeddingNode == null || !embeddingNode.isArray()) {
                throw new IllegalArgumentException("JSON must contain an 'embedding' array");
            }

            int size = embeddingNode.size();
            float[] vector = new float[size];

            for (int i = 0; i < size; i++) {
                vector[i] = (float) embeddingNode.get(i).asDouble();
            }

            LOG.debugf("Parsed vector of dimension %d from JSON", size);
            return vector;

        } catch (Exception e) {
            LOG.error("Failed to parse vector data from JSON", e);
            throw new RuntimeException("Failed to parse vector data: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a float array to JSON format for storage.
     *
     * @param vector the embedding vector
     * @return JSON string in the format {"embedding": [...]}
     */
    public static String toJsonString(float[] vector) {
        try {
            StringBuilder json = new StringBuilder("{\"embedding\": [");

            for (int i = 0; i < vector.length; i++) {
                if (i > 0) {
                    json.append(", ");
                }
                json.append(vector[i]);
            }

            json.append("]}");
            return json.toString();

        } catch (Exception e) {
            LOG.error("Failed to convert vector to JSON", e);
            throw new RuntimeException("Failed to convert vector to JSON: " + e.getMessage(), e);
        }
    }
}
