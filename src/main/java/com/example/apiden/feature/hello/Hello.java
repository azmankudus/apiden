package com.example.apiden.feature.hello;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public final record Hello(@NonNull String message) {
}
