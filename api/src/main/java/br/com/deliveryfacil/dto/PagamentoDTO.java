package br.com.deliveryfacil.dto;

import br.com.deliveryfacil.model.Pagamento;
import br.com.deliveryfacil.model.enums.FormaPagamento;
import br.com.deliveryfacil.model.enums.StatusPagamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class PagamentoDTO {
    private PagamentoDTO() {}

    public record Criar(@NotNull FormaPagamento forma) {}

    public record Webhook(@NotBlank String idTransacaoGateway, @NotNull StatusPagamento status) {}

    public record Resposta(Long id, Long idPedido, FormaPagamento forma, StatusPagamento status, BigDecimal valor,
                           String idTransacaoGateway, LocalDateTime dataCriacao) {
        public static Resposta de(Pagamento p) {
            return new Resposta(p.getIdPagamento(), p.getPedido().getIdPedido(), p.getForma(), p.getStatus(),
                    p.getValor(), p.getIdTransacaoGateway(), p.getDataCriacao());
        }
    }
}
