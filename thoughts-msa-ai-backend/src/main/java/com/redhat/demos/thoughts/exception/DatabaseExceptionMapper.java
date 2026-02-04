package com.redhat.demos.thoughts.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.logging.Logger;

@Provider
public class DatabaseExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = Logger.getLogger(DatabaseExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        ErrorResponse error = new ErrorResponse(
                "Database constraint violation occurred",
                Response.Status.CONFLICT.getStatusCode()
        );

        LOG.errorf(exception, "Database constraint violation [correlationId=%s]", error.correlationId);

        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .header("X-Correlation-ID", error.correlationId)
                .build();
    }
}
