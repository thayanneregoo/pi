package br.com.deliveryfacil.exception;

import org.springframework.http.HttpStatus;

public class NaoAutenticadoException extends ApiException {
    public NaoAutenticadoException(String mensagem) { super(HttpStatus.UNAUTHORIZED, mensagem); }
}
