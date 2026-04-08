package com.geo.survey.infrastructure.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("""
        hasAnyRole(T(com.geo.survey.domain.model.Role).ADMIN.name(),
                        T(com.geo.survey.domain.model.Role).SURVEYOR.name())
        """
)
public @interface IsAdminOrSurveyor {
}
