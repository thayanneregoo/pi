package br.com.deliveryfacil.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ErroResposta(OffsetDateTime timestamp, int status, String erro, String mensagem,
                           String caminho, List<String> detalhes) {
}
