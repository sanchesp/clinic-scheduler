package br.com.fiap.techchalleger.notificacaoservice.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMQConfigTest {

    private RabbitMQConfig config;

    @BeforeEach
    void setUp() throws Exception {
        config = new RabbitMQConfig();

        setField("exchangeName", "consultas.exchange");
        setField("queueName", "notificacoes.queue");
        setField("routingKeyCriada", "consulta.criada");
        setField("routingKeyAtualizada", "consulta.atualizada");
    }

    @Test
    void deveCriarExchange() {
        TopicExchange exchange = config.consultasExchange();

        assertNotNull(exchange);
        assertEquals("consultas.exchange", exchange.getName());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    void deveCriarQueue() {
        Queue queue = config.notificacoesQueue();

        assertNotNull(queue);
        assertEquals("notificacoes.queue", queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void deveCriarBindingParaConsultaCriada() {
        TopicExchange exchange = config.consultasExchange();
        Queue queue = config.notificacoesQueue();

        Binding binding = config.consultaCriadaBinding(
                queue,
                exchange
        );

        assertNotNull(binding);
        assertEquals("consultas.exchange", binding.getExchange());
        assertEquals("notificacoes.queue", binding.getDestination());
        assertEquals("consulta.criada", binding.getRoutingKey());
    }

    @Test
    void deveCriarBindingParaConsultaAtualizada() {
        TopicExchange exchange = config.consultasExchange();
        Queue queue = config.notificacoesQueue();

        Binding binding = config.consultaAtualizadaBinding(
                queue,
                exchange
        );

        assertNotNull(binding);
        assertEquals("consultas.exchange", binding.getExchange());
        assertEquals("notificacoes.queue", binding.getDestination());
        assertEquals("consulta.atualizada", binding.getRoutingKey());
    }

    @Test
    void deveCriarJsonMessageConverter() {
        MessageConverter converter = config.jsonMessageConverter();

        assertNotNull(converter);
    }

    private void setField(String fieldName, String value)
            throws Exception {

        Field field = RabbitMQConfig.class
                .getDeclaredField(fieldName);

        field.setAccessible(true);
        field.set(config, value);
    }
}