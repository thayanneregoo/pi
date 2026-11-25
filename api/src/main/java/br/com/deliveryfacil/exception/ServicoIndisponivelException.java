package br.com.deliveryfacil.exception;

import org.springframework.http.HttpStatus;

public class ServicoIndisponivelException extends ApiException {
    public ServicoIndisponivelException(String mensagem) { super(HttpStatus.SERVICE_UNAVAILABLE, mensagem); }
}
