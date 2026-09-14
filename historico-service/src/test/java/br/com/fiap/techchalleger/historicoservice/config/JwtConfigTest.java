package br.com.fiap.techchalleger.historicoservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtConfigTest {

    @Test
    void deveCriarJwtAuthenticationConverter() {

        JwtConfig jwtConfig = new JwtConfig();

        JwtAuthenticationConverter converter =
                jwtConfig.jwtAuthenticationConverter();

        assertNotNull(converter);
    }

    @Test
    void deveConverterRoleDoJwtParaAuthority() {

        JwtConfig jwtConfig = new JwtConfig();

        JwtAuthenticationConverter converter =
                jwtConfig.jwtAuthenticationConverter();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", "1",
                        "role", "MEDICO"
                )
        );

        Authentication authentication =
                converter.convert(jwt);

        assertNotNull(authentication);

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_MEDICO"))
        );
    }

    @Test
    void deveConverterRoleEnfermeiroParaAuthority() {

        JwtConfig jwtConfig = new JwtConfig();

        JwtAuthenticationConverter converter =
                jwtConfig.jwtAuthenticationConverter();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", "2",
                        "role", "ENFERMEIRO"
                )
        );

        Authentication authentication =
                converter.convert(jwt);

        assertNotNull(authentication);

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ENFERMEIRO"))
        );
    }
}