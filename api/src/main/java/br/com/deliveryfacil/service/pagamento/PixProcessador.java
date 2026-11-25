package br.com.deliveryfacil.service.pagamento;

import br.com.deliveryfacil.model.enums.FormaPagamento;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class PixProcessador extends ProcessadorPagamento {
    @Override public FormaPagamento forma() { return FormaPagamento.PIX; }
    @Override protected String prefixoTransacao() { return "PIX"; }
    @Override public Duration prazoExpiracao() { return Duration.ofMinutes(15); }
}
