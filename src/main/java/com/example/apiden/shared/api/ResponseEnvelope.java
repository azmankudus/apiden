package com.example.apiden.shared.api;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;
import java.util.Map;

@Serdeable
final record ResponseEnvelope(Object data, List<ApiError> errors, Map<String, Object> meta) {
}

@Serdeable
final record ApiError(String code, String message, Object detail) {
}
