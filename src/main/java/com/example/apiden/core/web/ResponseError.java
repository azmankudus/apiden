package com.example.apiden.core.web;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Represents an individual error entry within the {@link ResponseEnvelope}.
 * 
 * @param code the machine-readable error code (e.g., 00000404)
 * @param message the human-readable summary of the error
 * @param detail optional additional information, often used for field-level validation errors
 */
@Serdeable
public final record ResponseError(String code, String message, Object detail) {
}
