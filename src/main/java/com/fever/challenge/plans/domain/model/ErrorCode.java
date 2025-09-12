package com.fever.challenge.plans.domain.model;

import lombok.Getter;

/**
 * Códigos de error devueltos por la API.
 */
@Getter
public enum ErrorCode {
    /** Parámetros ausentes o con tipos erróneos. */
    BAD_REQUEST("400"),
    /** No se encontraron planes para la ventana especificada. */
    NOT_FOUND("404"),
    /** Error genérico no controlado. */
    INTERNAL_ERROR("500");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

}
