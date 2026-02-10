package com.redhat.demos.evaluation.service;

import com.redhat.demos.evaluation.model.ThoughtEvaluation;

import java.util.UUID;

/**
 * Service interface for evaluating thought content using AI embeddings and vector similarity.
 */
public interface EvaluationService {

    /**
     * Evaluates a thought by generating embeddings and comparing against negative vectors.
     *
     * @param thoughtId the unique identifier of the thought
     * @param thoughtContent the text content to evaluate
     * @return ThoughtEvaluation entity with the evaluation result
     */
    ThoughtEvaluation evaluateThought(UUID thoughtId, String thoughtContent);
}
