package br.com.deliveryfacil.dto;

import br.com.deliveryfacil.model.Avaliacao;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public final class AvaliacaoDTO {
    private AvaliacaoDTO() {}

    public record Criar(@NotNull @Min(1) @Max(5) Integer nota, String comentario) {}

    public record Responder(@NotBlank String resposta) {}

    public record Resposta(Long id, Long idPedido, int nota, String comentario, String respostaRestaurante,
                           LocalDateTime dataAvaliacao) {
        public static Resposta de(Avaliacao a) {
            return new Resposta(a.getIdAvaliacao(), a.getPedido().getIdPedido(), a.getNota(), a.getComentario(),
                    a.getRespostaRestaurante(), a.getDataAvaliacao());
        }
    }
}
