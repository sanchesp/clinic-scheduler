package br.com.fiap.techchalleger.historicoservice.repository;

import br.com.fiap.techchalleger.historicoservice.entity.Consulta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ConsultaRepositoryTest {

    @Autowired
    private ConsultaRepository consultaRepository;

    @BeforeEach
    void setUp() {
        consultaRepository.deleteAll();

        consultaRepository.saveAll(List.of(
                criarConsulta(
                        1L,
                        2L,
                        LocalDateTime.of(2026, 9, 15, 14, 0),
                        "AGENDADA",
                        "Consulta de rotina",
                        null
                ),
                criarConsulta(
                        1L,
                        2L,
                        LocalDateTime.of(2026, 9, 20, 10, 0),
                        "AGENDADA",
                        "Retorno",
                        null
                ),
                criarConsulta(
                        2L,
                        2L,
                        LocalDateTime.of(2026, 9, 18, 9, 0),
                        "REALIZADA",
                        "Consulta paciente 2",
                        "Diagnóstico inicial"
                ),
                criarConsulta(
                        1L,
                        3L,
                        LocalDateTime.of(2026, 9, 25, 16, 0),
                        "AGENDADA",
                        "Consulta com outro médico",
                        null
                )
        ));
    }

    @Test
    void deveBuscarConsultasDoPacienteOrdenadasPorDataDecrescente() {
        List<Consulta> resultado =
                consultaRepository
                        .findByPacienteIdOrderByDataHoraDesc(1L);

        assertEquals(3, resultado.size());

        assertEquals(
                LocalDateTime.of(2026, 9, 25, 16, 0),
                resultado.get(0).getDataHora()
        );

        assertEquals(
                LocalDateTime.of(2026, 9, 20, 10, 0),
                resultado.get(1).getDataHora()
        );

        assertEquals(
                LocalDateTime.of(2026, 9, 15, 14, 0),
                resultado.get(2).getDataHora()
        );
    }

    @Test
    void deveBuscarConsultasDoMedicoOrdenadasPorDataDecrescente() {
        List<Consulta> resultado =
                consultaRepository
                        .findByMedicoIdOrderByDataHoraDesc(2L);

        assertEquals(3, resultado.size());

        assertEquals(
                LocalDateTime.of(2026, 9, 20, 10, 0),
                resultado.get(0).getDataHora()
        );

        assertEquals(
                LocalDateTime.of(2026, 9, 18, 9, 0),
                resultado.get(1).getDataHora()
        );

        assertEquals(
                LocalDateTime.of(2026, 9, 15, 14, 0),
                resultado.get(2).getDataHora()
        );
    }

    @Test
    void deveBuscarApenasConsultasFuturasOrdenadasPorDataCrescente() {
        LocalDateTime dataReferencia =
                LocalDateTime.of(2026, 9, 17, 0, 0);

        List<Consulta> resultado =
                consultaRepository
                        .findByPacienteIdAndDataHoraAfterOrderByDataHoraAsc(
                                1L,
                                dataReferencia
                        );

        assertEquals(2, resultado.size());

        assertEquals(
                LocalDateTime.of(2026, 9, 20, 10, 0),
                resultado.get(0).getDataHora()
        );

        assertEquals(
                LocalDateTime.of(2026, 9, 25, 16, 0),
                resultado.get(1).getDataHora()
        );
    }

    @Test
    void deveRetornarListaVaziaQuandoPacienteNaoPossuirConsultas() {
        List<Consulta> resultado =
                consultaRepository
                        .findByPacienteIdOrderByDataHoraDesc(99L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveRetornarListaVaziaQuandoMedicoNaoPossuirConsultas() {
        List<Consulta> resultado =
                consultaRepository
                        .findByMedicoIdOrderByDataHoraDesc(99L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveRetornarListaVaziaQuandoPacienteNaoPossuirConsultasFuturas() {
        LocalDateTime dataReferencia =
                LocalDateTime.of(2026, 9, 30, 0, 0);

        List<Consulta> resultado =
                consultaRepository
                        .findByPacienteIdAndDataHoraAfterOrderByDataHoraAsc(
                                1L,
                                dataReferencia
                        );

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    private Consulta criarConsulta(
            Long pacienteId,
            Long medicoId,
            LocalDateTime dataHora,
            String status,
            String descricao,
            String diagnostico) {

        return new Consulta(
                null,
                pacienteId,
                medicoId,
                dataHora,
                status,
                descricao,
                diagnostico
        );
    }
}