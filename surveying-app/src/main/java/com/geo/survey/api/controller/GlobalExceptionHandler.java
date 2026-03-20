package com.geo.survey.api.controller;

import com.geo.survey.api.dto.ErrorResponse;
import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ParsingException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.exception.ValidationDataException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Map<Class<?>, HttpStatus> EXCEPTION_STATUS = Map.of(
            ConstraintViolationException.class, HttpStatus.BAD_REQUEST,
            ParsingException.class, HttpStatus.BAD_REQUEST,
            EntityNotFoundException.class, HttpStatus.NOT_FOUND,
            ResourceNotFoundException.class, HttpStatus.NOT_FOUND,
            DataIntegrityViolationException.class, HttpStatus.CONFLICT,
            BusinessRuleViolationException.class, HttpStatus.CONFLICT,
            ValidationDataException.class, HttpStatus.UNPROCESSABLE_ENTITY
    );

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
