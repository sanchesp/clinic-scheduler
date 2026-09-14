package br.com.fiap.techchalleger.notificacaoservice.consumer;

import br.com.fiap.techchalleger.notificacaoservice.dto.ConsultaEventoDTO;
import br.com.fiap.techchalleger.notificacaoservice.model.StatusConsulta;
import br.com.fiap.techchalleger.notificacaoservice.service.NotificacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class ConsultaEventConsumerIntegrationTest {

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitMQ =
            new RabbitMQContainer("rabbitmq:3.13-management");

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockitoSpyBean
    private NotificacaoService notificacaoService;

    @Test
    void deveReceberEventoRabbitMQEProcessarNotificacao() {

        ConsultaEventoDTO evento = new ConsultaEventoDTO(
                1L,
                1L,
                2L,
                LocalDateTime.of(2026, 9, 15, 14, 0),
                StatusConsulta.AGENDADA,
                "Consulta de rotina",
                LocalDateTime.of(2026, 9, 13, 23, 0)
        );

        rabbitTemplate.convertAndSend(
                "consultas.exchange",
                "consulta.criada",
                evento
        );

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        verify(notificacaoService)
                                .processar(evento)
                );
    }
}