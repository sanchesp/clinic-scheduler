package br.com.fiap.techchalleger.historicoservice.config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public JwtDecoder jwtDecoder() {

        SecretKey key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        return NimbusJwtDecoder
                .withSecretKey(key)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        var authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        var converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        converter.setPrincipalClaimName("userId");

        return converter;
    }
}
