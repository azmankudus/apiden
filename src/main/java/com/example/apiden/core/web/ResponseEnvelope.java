package com.example.apiden.core.web;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;
import java.util.Map;

/**
 * The standard outgoing API response envelope.
 */
@Serdeable
public final record ResponseEnvelope(Object data, List<ResponseError> errors, Map<String, Object> meta) {
}