package com.geo.survey.domain.service;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.Role;
import com.geo.survey.domain.model.User;
import com.geo.survey.infrastructure.database.repository.UserRepository;
import com.geo.survey.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final Clock clock;

    public User save(User user) {
        validateEmailUniqueness(user.getEmail());
        User registered = User.register(
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                user.getRole(),
                user.getCompany(),
                clock
        );
        return mapper.toDomain(userRepository.save(mapper.toEntity(registered)));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(mapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email [%s] does not exist".formatted(email))
                );
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .map(mapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with userId [%s] does not exist".formatted(userId))
                );
    }

    public void deleteUser(User user) {
        User deleted = user.delete(clock);
        userRepository.save(mapper.toEntity(deleted));
    }

    public User changeRole(String email, Role role) {
        User updated = findByEmail(email).changeRole(role);
        return mapper.toDomain(userRepository.save(mapper.toEntity(updated)));
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleViolationException(
                    "User with email [%s] already exists".formatted(email));
        }
    }

    public long countActiveAdmins(Long id) {
        return userRepository.countActiveAdminsByCompanyId(id);
    }
}
