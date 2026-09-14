package br.com.fiap.techchallenge.agendamento.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private String secret;
    private long expiration;
    private String validToken;

    @BeforeEach
    void setUp() {
        secret = Base64.getEncoder().encodeToString("chave-secreta-super-segura-para-testes-jwt-123456".getBytes());
        expiration = 3600000;

        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiration", expiration);

        validToken = jwtService.generateToken("user@test.com", "MEDICO", 1L);
    }

    @Test
    void deveGerarTokenComSucesso() {
        String token = jwtService.generateToken("joao_silva", "PACIENTE", 1L);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void deveExtrairNomeDeUsuarioDoToken() {
        String username = "dr_carlos";
        String role = "MEDICO";

        String token = jwtService.generateToken(username, role, 1L);

        String usernameExtraido = jwtService.extractUsername(token);

        assertEquals(username, usernameExtraido);
    }

    @Test
    void deveExtrairPapelDoToken() {
        String username = "joao";
        String role = "PACIENTE";

        String token = jwtService.generateToken(username, role, 1L);

        String roleExtraido = jwtService.extractRole(token);

        assertEquals(role, roleExtraido);
    }

    @Test
    void deveValidarTokenValido() {
        String token = jwtService.generateToken("usuario", "MEDICO", 1L);

        boolean valido = jwtService.isValid(token);

        assertTrue(valido);
    }

    @Test
    void deveInvalidarTokenComSignaturaDiferente() {
        String token = jwtService.generateToken("usuario", "MEDICO", 1L);

        String tokenModificado = token.substring(0, token.length() - 10) + "MODIFICADO";

        boolean valido = jwtService.isValid(tokenModificado);

        assertFalse(valido);
    }

    @Test
    void deveInvalidarTokenComAlgoritmioDiferente() {
        String secret2 = Base64.getEncoder().encodeToString(
            "chave-secreta-super-segura-para-testes-jwt-123456-com-mais-bytes-1234567890".getBytes()
        );

        String token = Jwts.builder()
                .subject("usuario")
                .claim("role", "MEDICO")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret2)), Jwts.SIG.HS256)
                .compact();

        boolean valido = jwtService.isValid(token);

        assertFalse(valido);
    }

    @Test
    void deveInvalidarTokenVazio() {
        boolean valido = jwtService.isValid("");

        assertFalse(valido);
    }

    @Test
    void deveInvalidarTokenNulo() {
        boolean valido = jwtService.isValid(null);

        assertFalse(valido);
    }

    @Test
    void deveConterClaimsCorretosNoToken() {
        String username = "usuario_teste";
        String role = "ENFERMEIRO";

        String token = jwtService.generateToken(username, role, 1L);

        String usuarioExtraido = jwtService.extractUsername(token);
        String roleExtraido = jwtService.extractRole(token);

        assertEquals(username, usuarioExtraido);
        assertEquals(role, roleExtraido);
    }

    @Test
    void deveGerarTokensDiferentesParaNomesDeUsuarioDiferentes() {
        String token1 = jwtService.generateToken("usuario1", "MEDICO", 1L);
        String token2 = jwtService.generateToken("usuario2", "MEDICO", 1L);

        assertNotEquals(token1, token2);
    }

    @Test
    void deveGerarTokensDiferentesParaRolesDiferentes() {
        String token1 = jwtService.generateToken("usuario", "MEDICO", 1L);
        String token2 = jwtService.generateToken("usuario", "PACIENTE", 1L);

        assertNotEquals(token1, token2);
    }

    @Test
    void deveConterTresPartesNoToken() {
        String token = jwtService.generateToken("usuario", "MEDICO", 1L);

        String[] partes = token.split("\\.");

        assertEquals(3, partes.length);
    }

    @Test
    void deveConterIssuedAtNoToken() {
        String token = jwtService.generateToken("usuario", "MEDICO", 1L);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertNotNull(claims.getIssuedAt());
    }

    @Test
    void deveConterExpirationNoToken() {
        String token = jwtService.generateToken("usuario", "MEDICO", 1L);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertNotNull(claims.getExpiration());
    }

    @Test
    void deveExtrairCaretoCorretoDoToken() {
        String roleEsperada = "MEDICO";
        String token = jwtService.generateToken("usuario", roleEsperada, 1L);

        String roleExtraida = jwtService.extractRole(token);

        assertEquals(roleEsperada, roleExtraida);
    }

    @Test
    void naoDeveValidarTokenComFomatoInvalido() {
        String tokenInvalido = "token-invalido";

        boolean valido = jwtService.isValid(tokenInvalido);

        assertFalse(valido);
    }

    @Test
    void deveExtrairUsernameCorretoDoToken() {
        String usernameEsperado = "joao_silva";
        String token = jwtService.generateToken(usernameEsperado, "PACIENTE", 1L);

        String usernameExtraido = jwtService.extractUsername(token);

        assertEquals(usernameEsperado, usernameExtraido);
    }

    @Test
    void deveValidarTokenComMultiplosClaims() {
        String token = jwtService.generateToken("usuario", "MEDICO", 1L);

        String username = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);
        boolean valido = jwtService.isValid(token);

        assertTrue(valido);
        assertEquals("usuario", username);
        assertEquals("MEDICO", role);
    }

}
