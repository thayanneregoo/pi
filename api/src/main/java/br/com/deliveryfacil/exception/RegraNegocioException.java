package br.com.deliveryfacil.exception;

import org.springframework.http.HttpStatus;

public class RegraNegocioException extends ApiException {
    public RegraNegocioException(String mensagem) { super(HttpStatus.UNPROCESSABLE_ENTITY, mensagem); }
}
