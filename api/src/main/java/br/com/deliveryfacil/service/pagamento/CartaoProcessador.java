package br.com.deliveryfacil.service.pagamento;

import br.com.deliveryfacil.model.enums.FormaPagamento;
import org.springframework.stereotype.Component;

@Component
public class CartaoProcessador extends ProcessadorPagamento {
    @Override public FormaPagamento forma() { return FormaPagamento.CARTAO; }
    @Override protected String prefixoTransacao() { return "CRD"; }
}
