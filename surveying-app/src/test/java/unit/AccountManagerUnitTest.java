package unit;

import com.geo.survey.domain.service.AccountManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class AccountManagerUnitTest {

    @InjectMocks
    private AccountManager accountManager;

    @Test
    void shouldThrowException_WhenRegisterUserDataIsNull() {
        //given, when, then
        assertThatThrownBy(() -> accountManager.registerUser(null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowException_WhenRegisterCompanyDataIsNull() {
        //given, when, then
        assertThatThrownBy(() -> accountManager.registerCompanyWithAdmin(null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
