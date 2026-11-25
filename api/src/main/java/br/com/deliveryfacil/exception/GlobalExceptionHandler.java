package br.com.deliveryfacil.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.List;

/** Tratamento centralizado de erros 4xx e 5xx com corpo JSON padronizado. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> aoLancarApiException(ApiException ex, WebRequest req) {
        return montar(ex.getStatus(), ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> aoViolarIntegridade(DataIntegrityViolationException ex, WebRequest req) {
        log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getMessage());
        return montar(HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade (registro duplicado ou em uso).", req, List.of());
    }

    @ExceptionHandler({DataAccessResourceFailureException.class, CannotCreateTransactionException.class})
    public ResponseEntity<Object> aoFalharBanco(Exception ex, WebRequest req) {
        log.error("Banco de dados indisponível", ex);
        return montar(HttpStatus.SERVICE_UNAVAILABLE, "Banco de dados temporariamente indisponível.", req, List.of());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> aoReceberParametroInvalido(Exception ex, WebRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "Parâmetro inválido.", req, List.of(String.valueOf(ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> aoFalharInesperadamente(Exception ex, WebRequest req) {
        log.error("Erro inesperado", ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.", req, List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).toList();
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos na requisição.", request, detalhes);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return montar(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou malformado.", request, List.of());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        return montar(HttpStatus.valueOf(statusCode.value()), ex.getMessage(), request, List.of());
    }

    private ResponseEntity<Object> montar(HttpStatus status, String mensagem, WebRequest req, List<String> detalhes) {
        String caminho = req instanceof ServletWebRequest s ? s.getRequest().getRequestURI() : "";
        return ResponseEntity.status(status).body(new ErroResposta(
                OffsetDateTime.now(), status.value(), status.getReasonPhrase(), mensagem, caminho, detalhes));
    }
}
