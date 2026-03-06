package com.example.apiden.feature.hello;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Data transfer object for a hello message.
 * 
 * @param message The greeting message string.
 */
@Serdeable
public final record Hello(@NonNull String message) {
}
