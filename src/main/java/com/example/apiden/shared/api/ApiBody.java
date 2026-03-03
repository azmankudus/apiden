package com.example.apiden.shared.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.micronaut.core.bind.annotation.Bindable;

/**
 * Annotation used to bind the body of an API request to a specific parameter or field.
 * This is typically used in conjunction with {@link ApiBodyBinder} to extract specific
 * parts of the incoming JSON envelope.
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Bindable
public @interface ApiBody {
}
