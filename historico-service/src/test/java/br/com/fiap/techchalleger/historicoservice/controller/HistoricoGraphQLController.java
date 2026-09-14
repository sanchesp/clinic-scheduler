package br.com.fiap.techchalleger.historicoservice.controller;

import br.com.fiap.techchalleger.historicoservice.dto.ConsultaResponse;
import br.com.fiap.techchalleger.historicoservice.exception.AcessoNegadoException;
import br.com.fiap.techchalleger.historicoservice.exception.ConsultaNotFoundException;
import br.com.fiap.techchalleger.historicoservice.service.HistoricoService;
import graphql.GraphQLError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricoGraphQLControllerTest {

    @Mock
    private HistoricoService historicoService;

    private HistoricoGraphQLController controller;

    @BeforeEach
    void setUp() {
        controller = new HistoricoGraphQLController(historicoService);
    }

    @AfterEach
    void limparSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveBuscarHistoricoDoPaciente() {

        Long pacienteId = 1L;

        Authentication authentication =
                criarAuthentication("PACIENTE", "1");

        configurarAuthentication(authentication);

        ConsultaResponse response = criarConsultaResponse();

        when(historicoService.buscarHistoricoPaciente(
                pacienteId,
                authentication
        )).thenReturn(List.of(response));

        List<ConsultaResponse> resultado =
                controller.historicoPaciente(pacienteId);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(response, resultado.get(0));

        verify(historicoService)
                .buscarHistoricoPaciente(
                        pacienteId,
                        authentication
                );
    }

    @Test
    void deveBuscarHistoricoDoMedico() {

        Long medicoId = 2L;

        ConsultaResponse response = criarConsultaResponse();

        when(historicoService.buscarHistoricoMedico(medicoId))
                .thenReturn(List.of(response));

        List<ConsultaResponse> resultado =
                controller.historicoMedico(medicoId);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(response, resultado.get(0));

        verify(historicoService)
                .buscarHistoricoMedico(medicoId);
    }

    @Test
    void deveBuscarConsultasFuturasDoPaciente() {

        Long pacienteId = 1L;

        Authentication authentication =
                criarAuthentication("PACIENTE", "1");

        configurarAuthentication(authentication);

        ConsultaResponse response = criarConsultaResponse();

        when(historicoService.buscarProximasConsultas(
                pacienteId,
                authentication
        )).thenReturn(List.of(response));

        List<ConsultaResponse> resultado =
                controller.consultasFuturas(pacienteId);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(response, resultado.get(0));

        verify(historicoService)
                .buscarProximasConsultas(
                        pacienteId,
                        authentication
                );
    }

    @Test
    void deveBuscarConsultaPorId() {

        Long consultaId = 1L;

        Authentication authentication =
                criarAuthentication("MEDICO", "2");

        configurarAuthentication(authentication);

        ConsultaResponse response = criarConsultaResponse();

        when(historicoService.buscarPorId(
                consultaId,
                authentication
        )).thenReturn(response);

        ConsultaResponse resultado =
                controller.consulta(consultaId);

        assertNotNull(resultado);
        assertEquals(response, resultado);

        verify(historicoService)
                .buscarPorId(
                        consultaId,
                        authentication
                );
    }

    @Test
    void deveTratarConsultaNotFoundException() {

        String mensagem = "10";

        ConsultaNotFoundException exception =
                new ConsultaNotFoundException(mensagem);

        GraphQLError error =
                controller.handleConsultaNotFound(exception);

        assertNotNull(error);
        assertNotNull(error.getMessage());
        assertNotNull(error.getErrorType());
    }

    @Test
    void deveTratarAcessoNegadoException() {

        String mensagem =
                "Paciente não pode consultar o histórico de outro paciente.";

        AcessoNegadoException exception =
                new AcessoNegadoException(mensagem);

        GraphQLError error =
                controller.handleAcessoNegado(exception);

        assertNotNull(error);
        assertEquals(mensagem, error.getMessage());
        assertNotNull(error.getErrorType());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistirHistorico() {

        Long pacienteId = 1L;

        Authentication authentication =
                criarAuthentication("PACIENTE", "1");

        configurarAuthentication(authentication);

        when(historicoService.buscarHistoricoPaciente(
                pacienteId,
                authentication
        )).thenReturn(List.of());

        List<ConsultaResponse> resultado =
                controller.historicoPaciente(pacienteId);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(historicoService)
                .buscarHistoricoPaciente(
                        pacienteId,
                        authentication
                );
    }

    private void configurarAuthentication(
            Authentication authentication
    ) {

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private Authentication criarAuthentication(
            String role,
            String usuarioId
    ) {

        return new UsernamePasswordAuthenticationToken(
                usuarioId,
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role
                        )
                )
        );
    }

    private ConsultaResponse criarConsultaResponse() {

        return new ConsultaResponse(
                1L,
                1L,
                2L,
                LocalDateTime.of(
                        2026,
                        9,
                        15,
                        14,
                        0
                ),
                "AGENDADA",
                "Consulta de rotina",
                "Diagnóstico inicial"
        );
    }
}