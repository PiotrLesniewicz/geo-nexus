package com.geo.survey.testconfig;

import com.geo.survey.infrastructure.security.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        SecurityConfiguration.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDenied.class
})
public class SecurityTestConfiguration {
}
