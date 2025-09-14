package com.fever.challenge.plans.adapters.in.rest.advice;

import com.fever.challenge.plans.adapters.in.rest.dto.SearchResponseDto;
import com.fever.challenge.plans.domain.model.ErrorCode;
import com.fever.challenge.plans.domain.model.ErrorDescription;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;

/**
 * Maps exceptions to the standard API error envelope:
 * <pre>{ "data": null, "error": { "code": "...", "message": "..." } }</pre>
 *
 * <p>Layer: inbound adapter (REST). The goal is to return clean client-facing errors
 * without leaking internals. Logging is intentionally minimal.</p>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Handles common client-side errors as HTTP 400.
     * Keeps logs concise and human: type + short message.
     */
    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<SearchResponseDto> handleBadRequest(Exception ex) {
        log.warn("400 Bad Request: {} - {}", ex.getClass().getSimpleName(), safeMessage(ex));
        return ResponseEntity.badRequest()
                .body(SearchResponseDto.error(ErrorCode.BAD_REQUEST, ErrorDescription.BAD_REQUEST));
    }

    /**
     * Catches everything else as HTTP 500.
     * We log the stack trace once here; the response remains generic.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SearchResponseDto> handleGeneric(Exception ex) {
        log.error("500 Internal Server Error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SearchResponseDto.error(ErrorCode.INTERNAL_ERROR, ErrorDescription.INTERNAL_ERROR));
    }

    // Returns a short, safe message for logs (avoids nulls and overly long payloads).
    private String safeMessage(Exception ex) {
        String message = ex.getMessage();
        if (Objects.isNull(message)) return "(no message)";
        return message.length() > 200 ? message.substring(0, 200) + "…" : message;
    }
}
