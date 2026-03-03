package com.example.apiden.shared.api;

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.sourcegen.annotations.Builder;
import io.micronaut.sourcegen.annotations.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * The top-level root object for the API communication envelope.
 * It contains metadata about the client context and the server processing context.
 *
 * @param client Information about the client context.
 * @param server Information about the server context.
 */
@Serdeable
@Builder
@ToString
final record ApiObject(ClientInfo client, ServerInfo server) {
        private static final Logger log = LoggerFactory.getLogger(ApiObject.class);

        /**
         * Canonical constructor for ApiObject.
         *
         * @param client The client info.
         * @param server The server info.
         */
        ApiObject(ClientInfo client, ServerInfo server) {
                this.client = client;
                this.server = server;
                log.trace("ApiObject instance created.");
        }
}

/**
 * Information about the client that initiated the request.
 *
 * @param http HTTP-specific client info.
 * @param request Request-specific client info.
 * @param response Response-specific client info.
 */
@Serdeable
@Builder
@ToString
final record ClientInfo(ClientHttpInfo http, ClientRequestInfo request, ClientResponseInfo response) {
        private static final Logger log = LoggerFactory.getLogger(ClientInfo.class);

        /**
         * Canonical constructor for ClientInfo.
         *
         * @param http The http info.
         * @param request The request info.
         * @param response The response info.
         */
        ClientInfo(ClientHttpInfo http, ClientRequestInfo request, ClientResponseInfo response) {
                this.http = http;
                this.request = request;
                this.response = response;
                log.trace("ClientInfo instance created.");
        }
}

/**
 * HTTP-specific metadata from the client side, such as headers.
 *
 * @param headers The HTTP headers.
 */
@Serdeable
@Builder
@ToString
final record ClientHttpInfo(Map<String, List<String>> headers) {
        private static final Logger log = LoggerFactory.getLogger(ClientHttpInfo.class);

        /**
         * Canonical constructor for ClientHttpInfo.
         *
         * @param headers The headers.
         */
        ClientHttpInfo(Map<String, List<String>> headers) {
                this.headers = headers;
                log.trace("ClientHttpInfo instance created with {} headers.", headers != null ? headers.size() : 0);
        }
}

/**
 * Transactional request information provided by the client.
 *
 * @param traceid The client-provided trace identifier.
 * @param timestamp The request timestamp.
 * @param body The request body payload.
 */
@Serdeable
@Builder
@ToString
final record ClientRequestInfo(String traceid, OffsetDateTime timestamp, Object body) {
        private static final Logger log = LoggerFactory.getLogger(ClientRequestInfo.class);

        /**
         * Canonical constructor for ClientRequestInfo.
         *
         * @param traceid The trace ID.
         * @param timestamp The timestamp.
         * @param body The body object.
         */
        ClientRequestInfo(String traceid, OffsetDateTime timestamp, Object body) {
                this.traceid = traceid;
                this.timestamp = timestamp;
                this.body = body;
                log.trace("ClientRequestInfo instance created with TraceID: {}", traceid);
        }
}

/**
 * Transactional response information as seen/expected by the client.
 *
 * @param timestamp The response timestamp.
 * @param duration The execution duration as string.
 */
@Serdeable
@Builder
@ToString
final record ClientResponseInfo(OffsetDateTime timestamp, String duration) {
        private static final Logger log = LoggerFactory.getLogger(ClientResponseInfo.class);

        /**
         * Canonical constructor for ClientResponseInfo.
         *
         * @param timestamp The timestamp.
         * @param duration The duration.
         */
        ClientResponseInfo(OffsetDateTime timestamp, String duration) {
                this.timestamp = timestamp;
                this.duration = duration;
                log.trace("ClientResponseInfo instance created.");
        }
}

/**
 * Information about the server's processing of the request.
 *
 * @param http HTTP-specific server info.
 * @param request Request-specific server info.
 * @param response Response-specific server info.
 */
@Serdeable
@Builder
@ToString
final record ServerInfo(ServerHttpInfo http, ServerRequestInfo request, ServerResponseInfo response) {
        private static final Logger log = LoggerFactory.getLogger(ServerInfo.class);

        /**
         * Canonical constructor for ServerInfo.
         *
         * @param http The http info.
         * @param request The request info.
         * @param response The response info.
         */
        ServerInfo(ServerHttpInfo http, ServerRequestInfo request, ServerResponseInfo response) {
                this.http = http;
                this.request = request;
                this.response = response;
                log.trace("ServerInfo instance created.");
        }
}

/**
 * HTTP-specific metadata from the server side.
 *
 * @param status The HTTP status code.
 * @param headers The HTTP headers.
 */
@Serdeable
@Builder
@ToString
final record ServerHttpInfo(Integer status, Map<String, List<String>> headers) {
        private static final Logger log = LoggerFactory.getLogger(ServerHttpInfo.class);

        /**
         * Canonical constructor for ServerHttpInfo.
         *
         * @param status The status code.
         * @param headers The headers.
         */
        ServerHttpInfo(Integer status, Map<String, List<String>> headers) {
                this.status = status;
                this.headers = headers;
                log.trace("ServerHttpInfo instance created with HTTP status: {}", status);
        }
}

/**
 * Transactional request information captured by the server.
 *
 * @param timestamp The request timestamp.
 * @param traceid The server-resolved trace identifier.
 */
@Serdeable
@Builder
@ToString
final record ServerRequestInfo(OffsetDateTime timestamp, String traceid) {
        private static final Logger log = LoggerFactory.getLogger(ServerRequestInfo.class);

        /**
         * Canonical constructor for ServerRequestInfo.
         *
         * @param timestamp The timestamp.
         * @param traceid The trace ID.
         */
        ServerRequestInfo(OffsetDateTime timestamp, String traceid) {
                this.timestamp = timestamp;
                this.traceid = traceid;
                log.trace("ServerRequestInfo instance created with TraceID: {}", traceid);
        }
}

/**
 * Transactional response information generated by the server.
 *
 * @param timestamp The response timestamp.
 * @param duration The execution duration.
 * @param status High-level status.
 * @param code Response code.
 * @param message Response message.
 * @param body Response body payload.
 */
@Serdeable
@Builder
@ToString
final record ServerResponseInfo(OffsetDateTime timestamp, String duration, String status, String code,
                String message, Object body) {
        private static final Logger log = LoggerFactory.getLogger(ServerResponseInfo.class);

        /**
         * Canonical constructor for ServerResponseInfo.
         *
         * @param timestamp The timestamp.
         * @param duration The duration.
         * @param status The status.
         * @param code The code.
         * @param message The message.
         * @param body The body.
         */
        ServerResponseInfo(OffsetDateTime timestamp, String duration, String status, String code,
                        String message, Object body) {
                this.timestamp = timestamp;
                this.duration = duration;
                this.status = status;
                this.code = code;
                this.message = message;
                this.body = body;
                log.trace("ServerResponseInfo instance created with status: {}, code: {}", status, code);
        }
}
