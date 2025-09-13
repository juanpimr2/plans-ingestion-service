package com.fever.challenge.plans.adapters.in.rest.advice;

import com.fever.challenge.plans.adapters.in.rest.dto.SearchResponseDto;
import com.fever.challenge.plans.domain.model.ErrorCode;
import com.fever.challenge.plans.domain.model.ErrorDescription;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

/**
 * Mapea excepciones a la respuesta estándar { data: null, error: { code, message } }.
 * Capa: adapter de entrada (REST).
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApiExceptionHandler {

    // 400 - petición inválida (parámetros faltantes o con tipo incorrecto)
    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<SearchResponseDto> handleBadRequest(Exception ex) {
        return ResponseEntity.badRequest()
                .body(SearchResponseDto.error(ErrorCode.BAD_REQUEST, ErrorDescription.BAD_REQUEST));
    }

    // 500 - error genérico no controlado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SearchResponseDto> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SearchResponseDto.error(ErrorCode.INTERNAL_ERROR, ErrorDescription.INTERNAL_ERROR));
    }
}
