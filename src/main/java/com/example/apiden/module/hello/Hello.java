package com.example.apiden.module.hello;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
final record Hello(@NonNull String message) {
}
