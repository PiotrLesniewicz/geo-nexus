package unit;

import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.User;
import com.geo.survey.domain.service.UserAuthService;
import com.geo.survey.infrastructure.database.entity.UserAuthEntity;
import com.geo.survey.infrastructure.database.repository.UserAuthRepository;
import com.geo.survey.infrastructure.mapper.UserAuthMapper;
import com.geo.survey.infrastructure.mapper.UserAuthMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

    private UserAuthService userAuthService;
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserAuthRepository userAuthRepository;
    @Mock
    private Clock clock;

    private static final Instant FIXED_INSTANT = Instant.parse("2020-10-03T12:00:00Z");

    @BeforeEach
    void setUp() {
        UserAuthMapper userAuthMapper = new UserAuthMapperImpl();
        passwordEncoder = new BCryptPasswordEncoder();
        userAuthService = new UserAuthService(userAuthRepository, userAuthMapper, passwordEncoder, clock);
    }

    @Test
    void shouldSaveUserAuthWithHashedPassword() {
        // given
        mockClock();
        User user = User.builder().id(1L).email("test@test.pl").build();
        String rawPassword = "tajnehaslo";

        // when
        userAuthService.save(user, rawPassword);

        // then
        ArgumentCaptor<UserAuthEntity> captor = ArgumentCaptor.forClass(UserAuthEntity.class);
        verify(userAuthRepository).save(captor.capture());
        UserAuthEntity saved = captor.getValue();

        assertThat(passwordEncoder.matches(rawPassword, saved.getPasswordHash())).isTrue();
    }

    @Test
    void shouldThrowException_WhenUserAuthNotFound() {
        // given
        Long userId = 99L;
        when(userAuthRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when then
        assertThatThrownBy(() -> userAuthService.findByUserId(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(userId));
    }

    private void mockClock() {
        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }
}