package br.com.fiap.techchalleger.historicoservice.service;

import br.com.fiap.techchalleger.historicoservice.dto.ConsultaResponse;
import br.com.fiap.techchalleger.historicoservice.entity.Consulta;
import br.com.fiap.techchalleger.historicoservice.exception.AcessoNegadoException;
import br.com.fiap.techchalleger.historicoservice.exception.ConsultaNotFoundException;
import br.com.fiap.techchalleger.historicoservice.repository.ConsultaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricoServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    private HistoricoService service;

    @BeforeEach
    void setUp() {
        service = new HistoricoService(consultaRepository);
    }

    @Test
    void deveBuscarHistoricoDoPacienteParaMedico() {
        Long pacienteId = 1L;
        Consulta consulta = criarConsulta();

        Authentication authentication =
                criarAuthentication("MEDICO", "2");

        when(consultaRepository
                .findByPacienteIdOrderByDataHoraDesc(pacienteId))
                .thenReturn(List.of(consulta));

        List<ConsultaResponse> resultado =
                service.buscarHistoricoPaciente(
                        pacienteId,
                        authentication
                );

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        ConsultaResponse response = resultado.get(0);

        assertEquals(consulta.getId(), response.id());
        assertEquals(consulta.getPacienteId(), response.pacienteId());
        assertEquals(consulta.getMedicoId(), response.medicoId());
        assertEquals(consulta.getDataHora(), response.dataHora());
        assertEquals(consulta.getStatus(), response.status());
        assertEquals(consulta.getDescricao(), response.descricao());
        assertEquals(consulta.getDiagnostico(), response.diagnostico());

        verify(consultaRepository)
                .findByPacienteIdOrderByDataHoraDesc(pacienteId);
    }

    @Test
    void deveBuscarHistoricoDoPacienteParaEnfermeiro() {
        Long pacienteId = 1L;
        Consulta consulta = criarConsulta();

        Authentication authentication =
                criarAuthentication("ENFERMEIRO", "3");

        when(consultaRepository
                .findByPacienteIdOrderByDataHoraDesc(pacienteId))
                .thenReturn(List.of(consulta));

        List<ConsultaResponse> resultado =
                service.buscarHistoricoPaciente(
                        pacienteId,
                        authentication
                );

        assertEquals(1, resultado.size());
        assertEquals(consulta.getId(), resultado.get(0).id());

        verify(consultaRepository)
                .findByPacienteIdOrderByDataHoraDesc(pacienteId);
    }

    @Test
    void deveBuscarHistoricoDoProprioPaciente() {
        Long pacienteId = 1L;
        Consulta consulta = criarConsulta();

        Authentication authentication =
                criarAuthentication("PACIENTE", "1");

        when(consultaRepository
                .findByPacienteIdOrderByDataHoraDesc(pacienteId))
                .thenReturn(List.of(consulta));

        List<ConsultaResponse> resultado =
                service.buscarHistoricoPaciente(
                        pacienteId,
                        authentication
                );

        assertEquals(1, resultado.size());
        assertEquals(pacienteId, resultado.get(0).pacienteId());

        verify(consultaRepository)
                .findByPacienteIdOrderByDataHoraDesc(pacienteId);
    }

    @Test
    void deveNegarPacienteConsultandoHistoricoDeOutroPaciente() {
        Long pacienteId = 2L;

        Authentication authentication =
                criarAuthentication("PACIENTE", "1");

        AcessoNegadoException exception =
                assertThrows(
                        AcessoNegadoException.class,
                        () -> service.buscarHistoricoPaciente(
                                pacienteId,
                                authentication
                        )
                );

        assertEquals(
                "Paciente não pode consultar o histórico de outro paciente.",
                exception.getMessage()
        );

        verifyNoInteractions(consultaRepository);
    }

    @Test
    void deveRetornarListaVaziaQuandoPacienteNaoPossuirHistorico() {
        Long pacienteId = 1L;

        Authentication authentication =
                criarAuthentication("PACIENTE", "1");

        when(consultaRepository
                .findByPacienteIdOrderByDataHoraDesc(pacienteId))
                .thenReturn(List.of());

        List<ConsultaResponse> resultado =
                service.buscarHistoricoPaciente(
                        pacienteId,
                        authentication
                );

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(consultaRepository)
                .findByPacienteIdOrderByDataHoraDesc(pacienteId);
    }

    @Test
    void deveBuscarHistoricoDoMedico() {
        Long medicoId = 2L;

        Consulta consulta = criarConsulta();

        when(consultaRepository
                .findByMedicoIdOrderByDataHoraDesc(medicoId))
                .thenReturn(List.of(consulta));

        List<ConsultaResponse> resultado =
                service.buscarHistoricoMedico(medicoId);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        assertEquals(
                consulta.getMedicoId(),
                resultado.get(0).medicoId()
        );

        assertEquals(
                consulta.getPacienteId(),
                resultado.get(0).pacienteId()
        );

        verify(consultaRepository)
                .findByMedicoIdOrderByDataHoraDesc(medicoId);
    }

    @Test
    void deveRetornarListaVaziaQuandoMedicoNaoPossuirHistorico() {
        Long medicoId = 2L;

        when(consultaRepository
                .findByMedicoIdOrderByDataHoraDesc(medicoId))
                .thenReturn(List.of());

        List<ConsultaResponse> resultado =
                service.buscarHistoricoMedico(medicoId);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(consultaRepository)
                .findByMedicoIdOrderByDataHoraDesc(medicoId);
    }

    @Test
    void deveBuscarProximasConsultasDoProprioPaciente() {
        Long pacienteId = 1L;

        Authentication authentication =
                criarAuthentication("PACIENTE", "1");

        Consulta consulta = criarConsulta();

        when(consultaRepository
                .findByPacienteIdAndDataHoraAfterOrderByDataHoraAsc(
                        eq(pacienteId),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(consulta));

        List<ConsultaResponse> resultado =
                service.buscarProximasConsultas(
                        pacienteId,
                        authentication
                );

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(consulta.getId(), resultado.get(0).id());

        verify(consultaRepository)
                .findByPacienteIdAndDataHoraAfterOrderByDataHoraAsc(
                        eq(pacienteId),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void deveNegarPacienteConsultandoConsultasFuturasDeOutroPaciente() {
        Long pacienteId = 2L;

        Authentication authentication =
                criarAuthentication("PACIENTE", "1");

        AcessoNegadoException exception =
                assertThrows(
                        AcessoNegadoException.class,
                        () -> service.buscarProximasConsultas(
                                pacienteId,
                                authentication
                        )
                );

        assertEquals(
                "Paciente não pode consultar as consultas futuras de outro paciente.",
                exception.getMessage()
        );

        verifyNoInteractions(consultaRepository);
    }

    @Test
    void devePermitirMedicoBuscarConsultasFuturasDoPaciente() {
        Long pacienteId = 1L;

        Authentication authentication =
                criarAuthentication("MEDICO", "2");

        Consulta consulta = criarConsulta();

        when(consultaRepository
                .findByPacienteIdAndDataHoraAfterOrderByDataHoraAsc(
                        eq(pacienteId),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(consulta));

        List<ConsultaResponse> resultado =
                service.buscarProximasConsultas(
                        pacienteId,
                        authentication
                );

        assertEquals(1, resultado.size());

        verify(consultaRepository)
                .findByPacienteIdAndDataHoraAfterOrderByDataHoraAsc(
                        eq(pacienteId),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void deveBuscarConsultaPorIdParaMedico() {
        Long consultaId = 1L;

        Authentication authentication =
                criarAuthentication("MEDICO", "2");

        Consulta consulta = criarConsulta();

        when(consultaRepository.findById(consultaId))
                .thenReturn(Optional.of(consulta));

        ConsultaResponse resultado =
                service.buscarPorId(
                        consultaId,
                        authentication
                );

        assertNotNull(resultado);
        assertEquals(consulta.getId(), resultado.id());
        assertEquals(consulta.getPacienteId(), resultado.pacienteId());
        assertEquals(consulta.getMedicoId(), resultado.medicoId());

        verify(consultaRepository)
                .findById(consultaId);
    }

    @Test
    void deveBuscarConsultaPorIdParaEnfermeiro() {
        Long consultaId = 1L;

        Authentication authentication =
                criarAuthentication("ENFERMEIRO", "3");

        Consulta consulta = criarConsulta();

        when(consultaRepository.findById(consultaId))
                .thenReturn(Optional.of(consulta));

        ConsultaResponse resultado =
                service.buscarPorId(
                        consultaId,
                        authentication
                );

        assertNotNull(resultado);
        assertEquals(consulta.getId(), resultado.id());

        verify(consultaRepository)
                .findById(consultaId);
    }

    @Test
    void devePermitirPacienteConsultarSuaPropriaConsulta() {
        Long consultaId = 1L;

        Authentication authentication =
                criarAuthentication("PACIENTE", "1");

        Consulta consulta = criarConsulta();

        when(consultaRepository.findById(consultaId))
                .thenReturn(Optional.of(consulta));

        ConsultaResponse resultado =
                service.buscarPorId(
                        consultaId,
                        authentication
                );

        assertNotNull(resultado);
        assertEquals(consulta.getId(), resultado.id());
        assertEquals(1L, resultado.pacienteId());

        verify(consultaRepository)
                .findById(consultaId);
    }

    @Test
    void deveNegarPacienteConsultandoConsultaDeOutroPaciente() {
        Long consultaId = 1L;

        Authentication authentication =
                criarAuthentication("PACIENTE", "2");

        Consulta consulta = criarConsulta();

        when(consultaRepository.findById(consultaId))
                .thenReturn(Optional.of(consulta));

        AcessoNegadoException exception =
                assertThrows(
                        AcessoNegadoException.class,
                        () -> service.buscarPorId(
                                consultaId,
                                authentication
                        )
                );

        assertEquals(
                "Paciente não pode consultar uma consulta de outro paciente.",
                exception.getMessage()
        );

        verify(consultaRepository)
                .findById(consultaId);
    }

    @Test
    void deveLancarExcecaoQuandoConsultaNaoExistir() {
        Long consultaId = 99L;

        Authentication authentication =
                criarAuthentication("MEDICO", "2");

        when(consultaRepository.findById(consultaId))
                .thenReturn(Optional.empty());

        assertThrows(
                ConsultaNotFoundException.class,
                () -> service.buscarPorId(
                        consultaId,
                        authentication
                )
        );

        verify(consultaRepository)
                .findById(consultaId);
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

    private Consulta criarConsulta() {
        return new Consulta(
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