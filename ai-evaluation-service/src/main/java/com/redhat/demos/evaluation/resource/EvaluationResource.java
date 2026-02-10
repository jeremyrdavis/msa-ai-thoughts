package com.redhat.demos.evaluation.resource;

import com.redhat.demos.evaluation.dto.EvaluationDTO;
import com.redhat.demos.evaluation.dto.EvaluationStatsDTO;
import com.redhat.demos.evaluation.exception.ErrorResponse;
import com.redhat.demos.evaluation.model.ThoughtEvaluation;
import com.redhat.demos.evaluation.model.ThoughtStatus;
import io.quarkus.panache.common.Page;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/evaluations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EvaluationResource {

    @GET
    public Response listEvaluations(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        List<ThoughtEvaluation> evaluations = ThoughtEvaluation.findAll()
                .page(Page.of(page, size))
                .list();

        List<EvaluationDTO> dtos = evaluations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return Response.ok(dtos).build();
    }

    @GET
    @Path("/thought/{thoughtId}")
    public Response getEvaluationByThoughtId(@PathParam("thoughtId") String thoughtIdStr) {
        try {
            UUID thoughtId = UUID.fromString(thoughtIdStr);

            ThoughtEvaluation evaluation = ThoughtEvaluation.find("thoughtId", thoughtId).firstResult();

            if (evaluation == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            return Response.ok(toDTO(evaluation)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid UUID format for thoughtId", Response.Status.BAD_REQUEST.getStatusCode()))
                    .build();
        }
    }

    @GET
    @Path("/stats")
    public Response getEvaluationStats() {
        long totalEvaluated = ThoughtEvaluation.count();
        long approvedCount = ThoughtEvaluation.count("status", ThoughtStatus.APPROVED);
        long rejectedCount = ThoughtEvaluation.count("status", ThoughtStatus.REJECTED);

        BigDecimal averageSimilarityScore = calculateAverageSimilarityScore();

        EvaluationStatsDTO stats = new EvaluationStatsDTO(
                totalEvaluated,
                approvedCount,
                rejectedCount,
                averageSimilarityScore
        );

        return Response.ok(stats).build();
    }

    private EvaluationDTO toDTO(ThoughtEvaluation evaluation) {
        return EvaluationDTO.builder()
                .id(evaluation.id)
                .thoughtId(evaluation.thoughtId)
                .status(evaluation.status)
                .similarityScore(evaluation.similarityScore)
                .evaluatedAt(evaluation.evaluatedAt)
                .build();
    }

    private BigDecimal calculateAverageSimilarityScore() {
        List<ThoughtEvaluation> evaluations = ThoughtEvaluation.listAll();

        if (evaluations.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = evaluations.stream()
                .map(e -> e.similarityScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(evaluations.size()), 4, RoundingMode.HALF_UP);
    }
}
