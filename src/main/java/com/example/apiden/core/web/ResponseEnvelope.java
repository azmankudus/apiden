package com.example.apiden.core.web;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.util.Map;

/**
 * The standard outgoing API response envelope.
 * 
 * <p>All API responses follow this structure to ensure consistency for consumers.
 * Meta-information can be toggled via configuration.</p>
 * 
 * @param data the primary success payload
 * @param errors a list of error entries if the request failed
 * @param meta a map for additional metadata (timestamps, trace IDs, etc.)
 */
@Serdeable
public final record ResponseEnvelope(Object data, List<ResponseError> errors, Map<String, Object> meta) {
}