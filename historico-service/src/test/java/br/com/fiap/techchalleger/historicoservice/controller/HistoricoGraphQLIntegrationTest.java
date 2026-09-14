package br.com.fiap.techchalleger.historicoservice.controller;

import br.com.fiap.techchalleger.historicoservice.entity.Consulta;
import br.com.fiap.techchalleger.historicoservice.repository.ConsultaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
class HistoricoGraphQLIntegrationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private ConsultaRepository consultaRepository;

    @BeforeEach
    void setUp() {
        consultaRepository.deleteAll();

        consultaRepository.saveAll(List.of(
                criarConsulta(
                        1L,
                        2L,
                        LocalDateTime.of(2026, 9, 10, 10, 0),
                        "REALIZADA",
                        "Consulta de rotina",
                        "Paciente em boas condições."
                ),
                criarConsulta(
                        1L,
                        2L,
                        LocalDateTime.of(2026, 9, 15, 14, 0),
                        "AGENDADA",
                        "Retorno",
                        null
                ),
                criarConsulta(
                        2L,
                        2L,
                        LocalDateTime.of(2026, 9, 20, 15, 0),
                        "AGENDADA",
                        "Consulta de outro paciente",
                        null
                )
        ));
    }

    @Test
    @WithMockUser(
            username = "1",
            roles = "PACIENTE"
    )
    void deveBuscarHistoricoDoProprioPaciente() {

        String document = """
                query {
                    historicoPaciente(pacienteId: 1) {
                        id
                        pacienteId
                        medicoId
                        dataHora
                        status
                        descricao
                        diagnostico
                    }
                }
                """;

        graphQlTester
                .document(document)
                .execute()
                .path("historicoPaciente")
                .entityList(Object.class)
                .hasSize(2);
    }

    @Test
    @WithMockUser(
            username = "1",
            roles = "PACIENTE"
    )
    void deveNegarHistoricoDeOutroPaciente() {

        String document = """
            query {
                historicoPaciente(pacienteId: 2) {
                    id
                    pacienteId
                    medicoId
                    dataHora
                    status
                }
            }
            """;

        graphQlTester
                .document(document)
                .execute()
                .errors()
                .satisfy(errors -> {
                    boolean acessoNegado = errors.stream()
                            .anyMatch(error ->
                                    "Paciente não pode consultar o histórico de outro paciente."
                                            .equals(error.getMessage())
                            );

                    assertEquals(true, acessoNegado);
                });
    }

    @Test
    @WithMockUser(
            username = "2",
            roles = "MEDICO"
    )
    void deveBuscarHistoricoDoMedico() {

        String document = """
                query {
                    historicoMedico(medicoId: 2) {
                        id
                        pacienteId
                        medicoId
                        dataHora
                        status
                        descricao
                        diagnostico
                    }
                }
                """;

        graphQlTester
                .document(document)
                .execute()
                .path("historicoMedico")
                .entityList(Object.class)
                .hasSize(3);
    }

    @Test
    @WithMockUser(
            username = "1",
            roles = "PACIENTE"
    )
    void deveBuscarConsultasFuturasDoPaciente() {

        String document = """
                query {
                    consultasFuturas(pacienteId: 1) {
                        id
                        pacienteId
                        medicoId
                        dataHora
                        status
                        descricao
                        diagnostico
                    }
                }
                """;

        graphQlTester
                .document(document)
                .execute()
                .path("consultasFuturas")
                .entityList(Object.class)
                .hasSize(1);
    }

    @Test
    @WithMockUser(
            username = "1",
            roles = "PACIENTE"
    )
    void deveBuscarConsultaPorId() {

        Consulta consulta = consultaRepository
                .findByPacienteIdOrderByDataHoraDesc(1L)
                .get(0);

        Long consultaId = consulta.getId();

        String document = """
                query {
                    consulta(id: %d) {
                        id
                        pacienteId
                        medicoId
                        dataHora
                        status
                        descricao
                        diagnostico
                    }
                }
                """.formatted(consultaId);

        graphQlTester
                .document(document)
                .execute()
                .path("consulta.id")
                .entity(Long.class)
                .isEqualTo(consultaId);
    }

    @Test
    @WithMockUser(
            username = "1",
            roles = "PACIENTE"
    )
    void deveNegarConsultaDeOutroPaciente() {

        Consulta consultaOutroPaciente = consultaRepository
                .findByPacienteIdOrderByDataHoraDesc(2L)
                .get(0);

        String document = """
                query {
                    consulta(id: %d) {
                        id
                        pacienteId
                        medicoId
                        dataHora
                        status
                    }
                }
                """.formatted(consultaOutroPaciente.getId());

        graphQlTester
                .document(document)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertEquals(1, errors.size());
                    assertEquals(
                            "Paciente não pode consultar uma consulta de outro paciente.",
                            errors.get(0).getMessage()
                    );
                });
    }

    @Test
    @WithMockUser(
            username = "2",
            roles = "MEDICO"
    )
    void devePermitirConsultaPorIdParaMedico() {

        Consulta consulta = consultaRepository
                .findByPacienteIdOrderByDataHoraDesc(1L)
                .get(0);

        String document = """
                query {
                    consulta(id: %d) {
                        id
                        pacienteId
                        medicoId
                        status
                    }
                }
                """.formatted(consulta.getId());

        graphQlTester
                .document(document)
                .execute()
                .path("consulta.id")
                .entity(Long.class)
                .isEqualTo(consulta.getId());
    }

    @Test
    @WithMockUser(
            username = "2",
            roles = "MEDICO"
    )
    void devePermitirHistoricoDoPacienteParaMedico() {

        String document = """
                query {
                    historicoPaciente(pacienteId: 1) {
                        id
                        pacienteId
                        medicoId
                        status
                    }
                }
                """;

        graphQlTester
                .document(document)
                .execute()
                .path("historicoPaciente")
                .entityList(Object.class)
                .hasSize(2);
    }

    private Consulta criarConsulta(
            Long pacienteId,
            Long medicoId,
            LocalDateTime dataHora,
            String status,
            String descricao,
            String diagnostico
    ) {
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