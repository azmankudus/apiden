package com.example.apiden.core.web;

import io.micronaut.serde.annotation.Serdeable;

/**
 * An individual API error entry.
 */
@Serdeable
public final record ResponseError(String code, String message, Object detail) {
}
