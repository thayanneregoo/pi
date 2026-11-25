package br.com.deliveryfacil.dto;

import br.com.deliveryfacil.model.HistoricoStatus;
import br.com.deliveryfacil.model.ItemOpcao;
import br.com.deliveryfacil.model.ItemPedido;
import br.com.deliveryfacil.model.Pedido;
import br.com.deliveryfacil.model.enums.StatusPedido;
import br.com.deliveryfacil.model.enums.TipoEntrega;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class PedidoDTO {
    private PedidoDTO() {}

    public record Criar(@NotNull Long idCliente, Long idEndereco, @NotNull TipoEntrega tipoEntrega) {}

    public record Item(@NotNull Long idProduto,
                       @NotNull @Min(value = 1, message = "Quantidade deve ser maior que zero") Integer quantidade,
                       @Size(max = 200) String observacao,
                       List<Long> opcoes) {}

    public record Cupom(@NotBlank String codigo) {}

    public record AlterarStatus(@NotNull StatusPedido status, @Size(max = 200) String motivo) {}

    public record Cancelamento(@Size(max = 200) String motivo) {}

    public record OpcaoItemResposta(Long idOpcao, String nome, BigDecimal precoAdicional) {
        public static OpcaoItemResposta de(ItemOpcao io) {
            return new OpcaoItemResposta(io.getOpcao().getIdOpcao(), io.getOpcao().getNome(), io.getPrecoAdicional());
        }
    }

    public record ItemResposta(Long idItem, Long idProduto, String produto, int quantidade, BigDecimal precoUnitario,
                               String observacao, List<OpcaoItemResposta> opcoes, BigDecimal subtotal) {
        public static ItemResposta de(ItemPedido i) {
            return new ItemResposta(i.getIdItem(), i.getProduto().getIdProduto(), i.getProduto().getNome(),
                    i.getQuantidade(), i.getPrecoUnitario(), i.getObservacao(),
                    i.getOpcoes().stream().map(OpcaoItemResposta::de).toList(), i.subtotal());
        }
    }

    public record Resposta(Long id, Long idCliente, Long idEndereco, TipoEntrega tipoEntrega,
                           LocalDateTime dataPedido, StatusPedido status, List<ItemResposta> itens,
                           BigDecimal subtotalProdutos, BigDecimal taxaEntrega, BigDecimal descontoCupom,
                           BigDecimal valorTotal, String motivoCancelamento) {
        public static Resposta de(Pedido p) {
            return new Resposta(p.getIdPedido(), p.getCliente().getIdCliente(),
                    p.getEndereco() == null ? null : p.getEndereco().getIdEndereco(), p.getTipoEntrega(),
                    p.getDataPedido(), p.getStatus(), p.getItens().stream().map(ItemResposta::de).toList(),
                    p.getSubtotalProdutos(), p.getTaxaEntrega(), p.getDescontoCupom(), p.getValorTotal(),
                    p.getMotivoCancelamento());
        }
    }

    public record HistoricoResposta(StatusPedido statusAnterior, StatusPedido statusNovo,
                                    LocalDateTime dataAlteracao, Long idFuncionario, String motivo) {
        public static HistoricoResposta de(HistoricoStatus h) {
            return new HistoricoResposta(h.getStatusAnterior(), h.getStatusNovo(), h.getDataAlteracao(),
                    h.getFuncionario() == null ? null : h.getFuncionario().getIdFuncionario(), h.getMotivo());
        }
    }
}
