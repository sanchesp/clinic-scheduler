package br.com.fiap.techchalleger.notificacaoservice.service;

import br.com.fiap.techchalleger.notificacaoservice.dto.ConsultaEventoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {

    private static final Logger log =
            LoggerFactory.getLogger(NotificacaoService.class);

    public void processar(ConsultaEventoDTO evento) {

        log.info(
                "Notificação recebida - Consulta: {}, Paciente: {}, Médico: {}, Data: {}, Status: {}",
                evento.consultaId(),
                evento.pacienteId(),
                evento.medicoId(),
                evento.dataHora(),
                evento.status()
        );
    }
}