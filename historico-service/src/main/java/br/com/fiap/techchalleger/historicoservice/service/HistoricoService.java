package br.com.fiap.techchalleger.historicoservice.service;

import br.com.fiap.techchalleger.historicoservice.dto.ConsultaResponse;
import br.com.fiap.techchalleger.historicoservice.entity.Consulta;
import br.com.fiap.techchalleger.historicoservice.exception.AcessoNegadoException;
import br.com.fiap.techchalleger.historicoservice.exception.ConsultaNotFoundException;
import br.com.fiap.techchalleger.historicoservice.repository.ConsultaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoricoService {

    private final ConsultaRepository consultaRepository;

    public HistoricoService(ConsultaRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    public List<ConsultaResponse> buscarHistoricoPaciente(
            Long pacienteId,
            Authentication authentication) {

        if (isPaciente(authentication)) {
            Long pacienteTokenId = Long.valueOf(authentication.getName());

            if (!pacienteTokenId.equals(pacienteId)) {
                throw new AcessoNegadoException(
                        "Paciente não pode consultar o histórico de outro paciente."
                );
            }
        }

        return consultaRepository
                .findByPacienteIdOrderByDataHoraDesc(pacienteId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ConsultaResponse> buscarHistoricoMedico(Long medicoId) {
        return consultaRepository
                .findByMedicoIdOrderByDataHoraDesc(medicoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ConsultaResponse> buscarProximasConsultas(
            Long pacienteId,
            Authentication authentication) {

        if (isPaciente(authentication)) {
            Long pacienteTokenId = Long.valueOf(authentication.getName());

            if (!pacienteTokenId.equals(pacienteId)) {
                throw new AcessoNegadoException(
                        "Paciente não pode consultar as consultas futuras de outro paciente."
                );
            }
        }

        return consultaRepository
                .findByPacienteIdAndDataHoraAfterOrderByDataHoraAsc(
                        pacienteId,
                        LocalDateTime.now()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ConsultaResponse buscarPorId(Long id, Authentication authentication) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new ConsultaNotFoundException(
                        "Consulta não encontrada: " + id
                ));

        if (isPaciente(authentication)) {
            Long pacienteTokenId = Long.valueOf(authentication.getName());

            if (!pacienteTokenId.equals(consulta.getPacienteId())) {
                throw new AcessoNegadoException(
                        "Paciente não pode consultar uma consulta de outro paciente."
                );
            }
        }

        return toResponse(consulta);
    }

    private boolean isPaciente(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PACIENTE"));
    }

    private ConsultaResponse toResponse(Consulta consulta) {
        return new ConsultaResponse(
                consulta.getId(),
                consulta.getPacienteId(),
                consulta.getMedicoId(),
                consulta.getDataHora(),
                consulta.getStatus(),
                consulta.getDescricao(),
                consulta.getDiagnostico()
        );
    }
}
