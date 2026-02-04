package com.redhat.demos.thoughts.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = Logger.getLogger(ValidationExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        ErrorResponse error = new ErrorResponse("Validation failed", Response.Status.BAD_REQUEST.getStatusCode());

        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            error.addFieldError(field, message);
        }

        LOG.warnf("Validation error [correlationId=%s]: %s", error.correlationId, exception.getMessage());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .header("X-Correlation-ID", error.correlationId)
                .build();
    }
}
