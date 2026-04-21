package com.geo.survey.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = NotSuperAdminValidator.class)
public @interface NotSuperAdmin {
    String message() default "Cannot assign SUPER_ADMIN role";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
