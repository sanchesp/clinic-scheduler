package br.com.fiap.techchalleger.historicoservice.exception;

public class ConsultaNotFoundException extends RuntimeException {

    public ConsultaNotFoundException(String id) {
        super("Consulta não encontrada: " + id);
    }
}
