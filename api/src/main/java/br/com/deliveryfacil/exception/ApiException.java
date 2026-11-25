package br.com.deliveryfacil.exception;

import org.springframework.http.HttpStatus;

/** Base abstrata das exceções de negócio: cada subclasse define o status HTTP correspondente. */
public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;

    protected ApiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
