package com.geo.survey.infrastructure.security;

import com.geo.survey.domain.model.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long userId;
    @Getter
    private final Long companyId;
    private final String email;
    private final String password;
    @Getter
    private final Role role;
    @Getter
    private final boolean userActive;
    @Getter
    private final boolean companyActive;
    private final boolean mustChangePassword;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !mustChangePassword;
    }

    @Override
    public boolean isEnabled() {
        return userActive && companyActive;
    }

}