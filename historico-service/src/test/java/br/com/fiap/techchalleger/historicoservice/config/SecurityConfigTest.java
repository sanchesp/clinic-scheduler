package br.com.fiap.techchalleger.historicoservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void deveCriarSecurityFilterChain() {
        assertNotNull(securityFilterChain);
    }

    @Test
    void deveCriarJwtAuthenticationConverter() {
        assertNotNull(jwtAuthenticationConverter);
    }
}