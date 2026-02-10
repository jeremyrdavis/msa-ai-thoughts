package com.redhat.demos.evaluation.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    private static final Logger LOG = Logger.getLogger(IllegalArgumentExceptionMapper.class);

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        ErrorResponse error = new ErrorResponse(
                exception.getMessage() != null ? exception.getMessage() : "Invalid argument provided",
                Response.Status.BAD_REQUEST.getStatusCode()
        );

        LOG.warnf("Invalid argument error [correlationId=%s]: %s", error.correlationId, exception.getMessage());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .header("X-Correlation-ID", error.correlationId)
                .build();
    }
}
