package com.example.apiden.core.web;

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.sourcegen.annotations.Builder;

/**
 * The standard incoming API request envelope.
 *
 * <p>
 * Shape: {@code {"data": {...}, "meta": {...}}}
 *
 * <p>
 * The {@code data} field contains the request payload.
 * The optional {@code meta} field may carry client-side metadata such as
 * {@code client_request_timestamp} and {@code client_trace_id}.
 *
 * @param data The request payload.
 * @param meta Optional client metadata.
 */
@Serdeable
@Builder
final record RequestEnvelope(Object data, Object meta) {
}