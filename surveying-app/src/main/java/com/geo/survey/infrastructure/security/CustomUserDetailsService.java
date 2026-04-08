package com.geo.survey.infrastructure.security;

import com.geo.survey.infrastructure.database.entity.UserAuthEntity;
import com.geo.survey.infrastructure.database.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAuthRepository userAuthRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userAuthRepository.findByUserEmail(email)
                .map(this::mapFromEntity)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: [%s]".formatted(email)));
    }

    private CustomUserDetails mapFromEntity(UserAuthEntity auth) {
        return new CustomUserDetails(
                auth.getUser().getId(),
                auth.getUser().getCompany().getId(),
                auth.getUser().getEmail(),
                auth.getPasswordHash(),
                auth.getUser().getRole(),
                auth.getUser().isActive(),
                auth.getUser().getCompany().isActive(),
                auth.isMustChange()
        );
    }
}
