package com.redhat.demos.thoughts.resource;

import com.redhat.demos.thoughts.dto.EvaluationDTO;
import com.redhat.demos.thoughts.dto.EvaluationStatsDTO;
import com.redhat.demos.thoughts.model.ThoughtEvaluation;
import com.redhat.demos.thoughts.model.ThoughtStatus;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Path("/ui/evaluations")
@Produces(MediaType.TEXT_HTML)
public class EvaluationUIResource {

    @Inject
    Template evaluations;

    @Inject
    Template stats;

    @GET
    public TemplateInstance getEvaluations(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        List<ThoughtEvaluation> evaluationList = ThoughtEvaluation.findAll()
                .page(page, size)
                .list();

        List<EvaluationDTO> evaluationDTOs = evaluationList.stream()
                .map(evaluation -> {
                    String thoughtContent = evaluation.thought != null
                            ? evaluation.thought.content
                            : "Content not available";
                    return EvaluationDTO.builder()
                            .id(evaluation.id)
                            .thoughtId(evaluation.thoughtId)
                            .thoughtContent(thoughtContent)
                            .status(evaluation.status)
                            .similarityScore(evaluation.similarityScore)
                            .evaluatedAt(evaluation.evaluatedAt)
                            .build();
                })
                .collect(Collectors.toList());

        long totalCount = ThoughtEvaluation.count();
        boolean hasMore = (page + 1) * size < totalCount;

        return evaluations
                .data("evaluations", evaluationDTOs)
                .data("page", page)
                .data("size", size)
                .data("hasMore", hasMore);
    }

    @GET
    @Path("/stats")
    public TemplateInstance getStats() {
        long totalEvaluated = ThoughtEvaluation.count();

        long approvedCount = ThoughtEvaluation.count("status", ThoughtStatus.APPROVED);
        long rejectedCount = ThoughtEvaluation.count("status", ThoughtStatus.REJECTED);

        BigDecimal averageSimilarityScore = calculateAverageSimilarityScore();

        EvaluationStatsDTO statsDTO = new EvaluationStatsDTO(
                totalEvaluated,
                approvedCount,
                rejectedCount,
                averageSimilarityScore
        );

        return stats.data("stats", statsDTO);
    }

    private BigDecimal calculateAverageSimilarityScore() {
        List<ThoughtEvaluation> allEvaluations = ThoughtEvaluation.listAll();

        if (allEvaluations.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = allEvaluations.stream()
                .map(evaluation -> evaluation.similarityScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(
                BigDecimal.valueOf(allEvaluations.size()),
                4,
                RoundingMode.HALF_UP
        );
    }
}
