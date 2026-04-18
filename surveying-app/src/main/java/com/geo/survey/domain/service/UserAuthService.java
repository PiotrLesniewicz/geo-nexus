package com.geo.survey.domain.service;

import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.User;
import com.geo.survey.domain.model.UserAuth;
import com.geo.survey.infrastructure.database.repository.UserAuthRepository;
import com.geo.survey.infrastructure.mapper.UserAuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserAuthRepository userAuthRepository;
    private final UserAuthMapper userAuthMapper;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public UserAuth findByUserId(Long id) {
        return userAuthRepository.findByUserId(id)
                .map(userAuthMapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Auth not found user"));

    }

    public void save(User user, String password) {
        UserAuth userAuth = UserAuth.register(user, passwordEncoder.encode(password), clock);
        userAuthRepository.save(userAuthMapper.toEntity(userAuth));
    }

    public boolean verifyPassword(Long userId, String rawPassword) {
        UserAuth userAuth = findByUserId(userId);
        return passwordEncoder.matches(rawPassword, userAuth.getPasswordHash());
    }

    public void updatePassword(Long userId, String newPassword) {
        UserAuth userAuth = findByUserId(userId);
        UserAuth updatedAuth = userAuth.updatePassword(passwordEncoder.encode(newPassword), clock);
        userAuthRepository.save(userAuthMapper.toEntity(updatedAuth));
    }
}
