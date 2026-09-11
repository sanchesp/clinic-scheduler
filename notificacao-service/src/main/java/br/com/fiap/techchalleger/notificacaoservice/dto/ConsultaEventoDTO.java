package br.com.fiap.techchalleger.notificacaoservice.dto;

import br.com.fiap.techchalleger.notificacaoservice.model.StatusConsulta;

import java.time.LocalDateTime;

public record ConsultaEventoDTO(
        Long consultaId,
        Long pacienteId,
        Long medicoId,
        LocalDateTime dataHora,
        StatusConsulta status,
        String observacoes,
        LocalDateTime ocorridoEm
) {
}
