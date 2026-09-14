package br.com.fiap.techchalleger.notificacaoservice.service;

import br.com.fiap.techchalleger.notificacaoservice.dto.ConsultaEventoDTO;
import br.com.fiap.techchalleger.notificacaoservice.model.StatusConsulta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificacaoServiceTest {

    private NotificacaoService notificacaoService;

    @BeforeEach
    void setUp() {
        notificacaoService = new NotificacaoService();
    }

    @Test
    void deveProcessarEventoSemLancarExcecao() {

        ConsultaEventoDTO evento = new ConsultaEventoDTO(
                1L,
                1L,
                2L,
                LocalDateTime.of(2026, 9, 15, 14, 0),
                StatusConsulta.AGENDADA,
                "Consulta de rotina",
                LocalDateTime.of(2026, 9, 13, 23, 0)
        );

        assertDoesNotThrow(() ->
                notificacaoService.processar(evento)
        );
    }
}