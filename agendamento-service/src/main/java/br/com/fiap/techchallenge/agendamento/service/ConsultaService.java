package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.dto.request.ConsultaRequest;
import br.com.fiap.techchallenge.agendamento.dto.request.ConsultaUpdateRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.ConsultaResponse;
import br.com.fiap.techchallenge.agendamento.exception.OperacaoInvalidaException;
import br.com.fiap.techchallenge.agendamento.exception.EntidadeNaoEncontradaException;
import br.com.fiap.techchallenge.agendamento.messaging.ConsultaEventoDTO;
import br.com.fiap.techchallenge.agendamento.model.*;
import br.com.fiap.techchallenge.agendamento.repository.ConsultaRepository;
import br.com.fiap.techchallenge.agendamento.repository.UsuarioRepository;
import br.com.fiap.techchallenge.agendamento.usecase.ConsultaUseCase;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ConsultaService implements ConsultaUseCase {

    private final ConsultaRepository consultaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.exchange}")
    private String exchange;

    @Value("${app.messaging.routing-key.criada}")
    private String routingKeyCriada;

    @Value("${app.messaging.routing-key.atualizada}")
    private String routingKeyAtualizada;

    public ConsultaService(ConsultaRepository consultaRepository, UsuarioRepository usuarioRepository, RabbitTemplate rabbitTemplate) {
        this.consultaRepository = consultaRepository;
        this.usuarioRepository = usuarioRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    public ConsultaResponse create(ConsultaRequest request) {
        Usuario paciente = usuarioRepository.findById(request.pacienteId())
                .filter(Paciente.class::isInstance)
                .orElseThrow(() -> new IllegalArgumentException(
                        "pacienteId informado não corresponde a um paciente válido"));

        Usuario medico = usuarioRepository.findById(request.medicoId())
                .filter(Medico.class::isInstance)
                .orElseThrow(() -> new IllegalArgumentException(
                        "medicoId informado não corresponde a um médico válido"));

        if (Objects.equals(paciente.getId(), medico.getId())) {
            throw new IllegalArgumentException(
                    "Paciente e médico não podem ser o mesmo usuário");
        }

        Usuario registradoPor = usuarioAutenticado();

        Consulta consulta = new Consulta();
        consulta.setPacienteId(paciente.getId());
        consulta.setMedicoId(medico.getId());
        consulta.setRegistradoPorId(registradoPor.getId());
        consulta.setDataHora(request.dataHora());
        consulta.setStatus(StatusConsulta.AGENDADA);
        consulta.setObservacoes(request.observacoes());
        consulta.setCriadoEm(LocalDateTime.now());
        consulta.setLastModifiedAt(LocalDateTime.now());

        consultaRepository.saveAndFlush(consulta);

        publicarEvento(consulta, routingKeyCriada);

        return ConsultaResponse.from(consulta);
    }

    @Override
    @Transactional
    public ConsultaResponse update(Long id, ConsultaUpdateRequest request) {
        Consulta consulta = buscarPorIdOuFalhar(id);

        if (consulta.getStatus() == StatusConsulta.CANCELADA) {
            throw new IllegalArgumentException(
                    "Não é possível atualizar uma consulta cancelada");
        }

        if (request.dataHora() != null) {
            consulta.setDataHora(request.dataHora());
        }

        if (request.status() != null) {
            consulta.setStatus(request.status());
        }

        if (request.observacoes() != null) {
            consulta.setObservacoes(request.observacoes());
        }

        consulta.setLastModifiedAt(LocalDateTime.now());

        consultaRepository.saveAndFlush(consulta);

        publicarEvento(consulta, routingKeyAtualizada);

        return ConsultaResponse.from(consulta);
    }

    @Override
    public ConsultaResponse findById(Long id) {
        Consulta consulta = buscarPorIdOuFalhar(id);
        garantirAcessoAoPaciente(consulta.getPacienteId());
        return ConsultaResponse.from(consulta);
    }

    @Override
    public List<ConsultaResponse> findAll() {
        return consultaRepository.findAll().stream()
                .map(ConsultaResponse::from)
                .toList();
    }

    @Override
    public List<ConsultaResponse> findByPaciente(Long pacienteId) {
        garantirAcessoAoPaciente(pacienteId);
        return consultaRepository.findByPacienteId(pacienteId).stream()
                .map(ConsultaResponse::from)
                .toList();
    }

    private Consulta buscarPorIdOuFalhar(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Consulta com id " + id + " não encontrada"));
    }


    private void garantirAcessoAoPaciente(Long pacienteId) {
        Usuario usuarioLogado = usuarioAutenticado();
        boolean ehPacienteDeOutraConsulta = usuarioLogado instanceof Paciente
                && !Objects.equals(usuarioLogado.getId(), pacienteId);

        if (ehPacienteDeOutraConsulta) {
            throw new OperacaoInvalidaException("Paciente só pode visualizar as próprias consultas");
        }
    }

    private Usuario usuarioAutenticado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário autenticado não encontrado: " + login));
    }

    private void publicarEvento(Consulta consulta, String routingKey) {
        ConsultaEventoDTO evento = new ConsultaEventoDTO(
                consulta.getId(),
                consulta.getPacienteId(),
                consulta.getMedicoId(),
                consulta.getDataHora(),
                consulta.getStatus(),
                consulta.getObservacoes(),
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(exchange, routingKey, evento);
    }
}
