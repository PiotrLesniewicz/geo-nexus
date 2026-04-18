package com.geo.survey.unit;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.Role;
import com.geo.survey.domain.model.User;
import com.geo.survey.domain.service.UserService;
import com.geo.survey.infrastructure.database.entity.UserEntity;
import com.geo.survey.infrastructure.database.repository.UserRepository;
import com.geo.survey.infrastructure.mapper.UserMapper;
import com.geo.survey.infrastructure.mapper.UserMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.geo.survey.testdata.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() {
        UserMapper userMapper = new UserMapperImpl();
        userService = new UserService(userRepository, userMapper, clock);
    }

    @Test
    void shouldCorrectlySaveUser_WhenUserDoesNotExist() {
        // given
        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        User user = userWithoutStatus();

        when(userRepository.existsByEmail(DEFAULT_EMAIL)).thenReturn(false);
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        // when
        User result = userService.save(user);

        // then
        assertNotNull(result);
        assertThat(result)
                .returns(DEFAULT_EMAIL, User::getEmail)
                .returns(fixedDateTime(), User::getRegisterAt)
                .returns(true, User::isActive);

        verify(userRepository).existsByEmail(DEFAULT_EMAIL);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowException_WhenEmailAlreadyExists() {
        // given
        User user = userWithoutStatus();

        when(userRepository.existsByEmail(DEFAULT_EMAIL)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.save(user))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining(DEFAULT_EMAIL);

        verify(userRepository).existsByEmail(DEFAULT_EMAIL);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_WhenUserNotFoundByEmail() {
        // given
        Long companyId = 1L;
        when(userRepository.findByEmailAndCompanyId(DEFAULT_EMAIL, companyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findByEmail(DEFAULT_EMAIL, companyId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(DEFAULT_EMAIL);

        verify(userRepository).findByEmailAndCompanyId(DEFAULT_EMAIL, companyId);
    }

    @Test
    void shouldChangeUserRole() {
        // given
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        // when
        User result = userService.changeRole(activeUser(), Role.ADMIN);

        // then
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowException_WhenChangingToTheSameRole() {
        // given, when, then
        User user = activeUser();
        assertThatThrownBy(() -> userService.changeRole(user, Role.SURVEYOR))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining(user.getEmail());
    }
}
