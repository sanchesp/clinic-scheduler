package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.dto.request.LoginRequest;
import br.com.fiap.techchallenge.agendamento.dto.response.AuthResponse;
import br.com.fiap.techchallenge.agendamento.exception.AutorizacaoInvalidaException;
import br.com.fiap.techchallenge.agendamento.model.Usuario;
import br.com.fiap.techchallenge.agendamento.repository.UsuarioRepository;
import br.com.fiap.techchallenge.agendamento.usecase.AuthUseCase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements AuthUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse authenticate(LoginRequest request) {

        Usuario usuario = usuarioRepository.findByLogin(request.login())
                .orElseThrow(AutorizacaoInvalidaException::new);

        if (usuario.getSenha() == null ||
                !passwordEncoder.matches(request.password(), usuario.getSenha())) {
            throw new AutorizacaoInvalidaException();
        }

        // Perfil derivado do tipo concreto (Medico/Enfermeiro/Paciente) e gravado no token
        String perfil = usuario.getClass().getSimpleName().toUpperCase();
        String token = jwtService.generateToken(usuario.getLogin(), perfil, usuario.getId());

        return new AuthResponse(token);
    }
}
