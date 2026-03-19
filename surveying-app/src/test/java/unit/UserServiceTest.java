package unit;

import com.geo.survey.domain.exception.*;
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

import static data.UserFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserService userService;
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;
    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
        userService = new UserService(userRepository, userMapper, clock);
    }

    @Test
    void shouldCorrectlySaveUser_WhenUserDoesNotExist() {
        // given
        mockClock();
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
        when(userRepository.findByEmail(DEFAULT_EMAIL)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findByEmail(DEFAULT_EMAIL))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(DEFAULT_EMAIL);

        verify(userRepository).findByEmail(DEFAULT_EMAIL);
    }

    @Test
    void shouldChangeUserRole() {
        // given
        when(userRepository.findByEmail(DEFAULT_EMAIL))
                .thenReturn(Optional.of(userMapper.toEntity(activeUser())));
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        // when
        User result = userService.changeRole(DEFAULT_EMAIL, Role.ADMIN);

        // then
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).findByEmail(DEFAULT_EMAIL);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowException_WhenChangingToTheSameRole() {
        // given
        when(userRepository.findByEmail(DEFAULT_EMAIL))
                .thenReturn(Optional.of(userMapper.toEntity(activeUser())));

        // when & then
        assertThatThrownBy(() -> userService.changeRole(DEFAULT_EMAIL, Role.SURVEYOR))
                .isInstanceOf(RoleException.class)
                .hasMessageContaining(DEFAULT_EMAIL);

        verify(userRepository).findByEmail(DEFAULT_EMAIL);
        verify(userRepository, never()).save(any());
    }

    private void mockClock() {
        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }
}
