package br.com.fiap.techchalleger.notificacaoservice.consumer;

import br.com.fiap.techchalleger.notificacaoservice.dto.ConsultaEventoDTO;
import br.com.fiap.techchalleger.notificacaoservice.service.NotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class ConsultaEventConsumerTest {

    private NotificacaoService notificacaoService;
    private ConsultaEventConsumer consumer;

    @BeforeEach
    void setUp() {
        notificacaoService = mock(NotificacaoService.class);
        consumer = new ConsultaEventConsumer(notificacaoService);
    }

    @Test
    void deveProcessarEventoRecebido() {
        ConsultaEventoDTO evento = mock(ConsultaEventoDTO.class);

        consumer.receber(evento);

        verify(notificacaoService).processar(evento);
    }

    @Test
    void deveEnviarExatamenteOMesmoEventoParaOService() {
        ConsultaEventoDTO evento = mock(ConsultaEventoDTO.class);

        consumer.receber(evento);

        ArgumentCaptor<ConsultaEventoDTO> captor =
                ArgumentCaptor.forClass(ConsultaEventoDTO.class);

        verify(notificacaoService).processar(captor.capture());

        assertSame(evento, captor.getValue());
    }

    @Test
    void naoDeveProcessarMaisDeUmaVez() {
        ConsultaEventoDTO evento = mock(ConsultaEventoDTO.class);

        consumer.receber(evento);

        verify(notificacaoService, times(1))
                .processar(evento);
    }
}