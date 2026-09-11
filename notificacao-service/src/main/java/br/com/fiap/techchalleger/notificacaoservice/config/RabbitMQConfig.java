package br.com.fiap.techchalleger.notificacaoservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Value("${app.messaging.exchange}")
    private String exchangeName;

    @Value("${app.messaging.queue}")
    private String queueName;

    @Value("${app.messaging.routing-key.criada}")
    private String routingKeyCriada;

    @Value("${app.messaging.routing-key.atualizada}")
    private String routingKeyAtualizada;

    @Bean
    public TopicExchange consultasExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue notificacoesQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding consultaCriadaBinding(
            Queue notificacoesQueue,
            TopicExchange consultasExchange) {

        return BindingBuilder
                .bind(notificacoesQueue)
                .to(consultasExchange)
                .with(routingKeyCriada);
    }

    @Bean
    public Binding consultaAtualizadaBinding(
            Queue notificacoesQueue,
            TopicExchange consultasExchange) {

        return BindingBuilder
                .bind(notificacoesQueue)
                .to(consultasExchange)
                .with(routingKeyAtualizada);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}