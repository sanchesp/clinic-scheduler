package br.com.fiap.techchalleger.notificacaoservice.consumer;

import br.com.fiap.techchalleger.notificacaoservice.dto.ConsultaEventoDTO;
import br.com.fiap.techchalleger.notificacaoservice.service.NotificacaoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConsultaEventConsumer {

    private final NotificacaoService notificacaoService;

    public ConsultaEventConsumer(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @RabbitListener(queues = "${app.messaging.queue}")
    public void receber(ConsultaEventoDTO evento) {
        notificacaoService.processar(evento);
    }
}