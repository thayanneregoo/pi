package br.com.deliveryfacil.service.pagamento;

import br.com.deliveryfacil.model.Pagamento;
import br.com.deliveryfacil.model.Pedido;
import br.com.deliveryfacil.model.enums.FormaPagamento;
import br.com.deliveryfacil.model.enums.StatusPagamento;

import java.time.Duration;
import java.util.UUID;

/**
 * Template Method: o fluxo de criação do pagamento é igual para qualquer forma; as subclasses
 * definem só o que muda (forma, prefixo da transação, prazo de expiração). Nenhum dado de cartão
 * passa por aqui: o gateway (simulado) devolve apenas um identificador de transação.
 */
public abstract class ProcessadorPagamento {

    public abstract FormaPagamento forma();

    protected abstract String prefixoTransacao();

    /** Prazo para o pagamento ser confirmado; null = sem expiração automática. */
    public Duration prazoExpiracao() { return null; }

    public final Pagamento criar(Pedido pedido) {
        Pagamento pg = new Pagamento();
        pg.setPedido(pedido);
        pg.setForma(forma());
        pg.setStatus(StatusPagamento.AGUARDANDO);
        pg.setValor(pedido.getValorTotal());
        pg.setIdTransacaoGateway(prefixoTransacao() + "-" + UUID.randomUUID());
        return pg;
    }
}
