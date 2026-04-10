package com.geo.survey.api.controller;

import com.geo.survey.api.dto.ErrorResponse;
import com.geo.survey.domain.exception.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    private static final Map<Class<?>, HttpStatus> EXCEPTION_STATUS = new LinkedHashMap<>();

    static {
        EXCEPTION_STATUS.put(ConstraintViolationException.class, HttpStatus.BAD_REQUEST);
        EXCEPTION_STATUS.put(ParsingException.class, HttpStatus.BAD_REQUEST);
        EXCEPTION_STATUS.put(EntityNotFoundException.class, HttpStatus.NOT_FOUND);
        EXCEPTION_STATUS.put(ResourceNotFoundException.class, HttpStatus.NOT_FOUND);
        EXCEPTION_STATUS.put(DataIntegrityViolationException.class, HttpStatus.CONFLICT);
        EXCEPTION_STATUS.put(BusinessRuleViolationException.class, HttpStatus.CONFLICT);
        EXCEPTION_STATUS.put(ValidationDataException.class, HttpStatus.UNPROCESSABLE_ENTITY);
        EXCEPTION_STATUS.put(UnauthorizedAccessException.class, HttpStatus.FORBIDDEN);
        EXCEPTION_STATUS.put(AccessDeniedException.class, HttpStatus.FORBIDDEN);
        EXCEPTION_STATUS.put(AuthenticationException.class, HttpStatus.UNAUTHORIZED);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @NonNull Exception ex,
            @Nullable Object body,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        final String errorId = UUID.randomUUID().toString();
        log.error("Exception: ID={}, HttpStatus={}", errorId, status, ex);
        return super.handleExceptionInternal(
                ex,
                ErrorResponse.of(errorId),
                headers,
                status,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handle(Exception ex) {
        return doHandle(ex, getHttpStatusFromException(ex.getClass()));
    }

    private ResponseEntity<Object> doHandle(Exception ex, HttpStatus status) {
        final String errorId = UUID.randomUUID().toString();
        log.error("Exception: ID={}, HttpStatus={}", errorId, status, ex);
        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(errorId));
    }

    private HttpStatus getHttpStatusFromException(Class<? extends Exception> ex) {
        for (Map.Entry<Class<?>, HttpStatus> entry : EXCEPTION_STATUS.entrySet()) {
            if (entry.getKey().isAssignableFrom(ex)) {
                return entry.getValue();
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
