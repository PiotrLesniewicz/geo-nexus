package com.geo.survey.api.validation;

import com.geo.survey.domain.model.Role;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotSuperAdminValidator implements ConstraintValidator<NotSuperAdmin, Role> {
    @Override
    public boolean isValid(Role role, ConstraintValidatorContext context) {
        return role != Role.SUPER_ADMIN;
    }
}
