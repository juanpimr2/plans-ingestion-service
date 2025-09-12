package com.fever.challenge.plans.domain.model;

import lombok.Getter;

/**
 * Descripciones de los códigos de error.
 */
@Getter
public enum ErrorDescription {
    BAD_REQUEST("The request was not correctly formed (missing required parameters, wrong types...)."),
    NOT_FOUND("No plans were found for the specified time window."),
    INTERNAL_ERROR("An unexpected error occurred.");

    private final String description;

    ErrorDescription(String description) {
        this.description = description;
    }

}
