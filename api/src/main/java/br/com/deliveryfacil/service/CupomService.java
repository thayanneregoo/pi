package br.com.deliveryfacil.service;

import br.com.deliveryfacil.exception.RegraNegocioException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/** Cupons fixos de demonstração (o modelo do documento de visão não define tabela de cupons). */
@Service
public class CupomService {
    private static final Map<String, BigDecimal> CUPONS = Map.of(
            "DELIVERY5", new BigDecimal("5.00"),
            "PRIMEIRA10", new BigDecimal("10.00"));

    public BigDecimal valorDesconto(String codigo) {
        BigDecimal valor = CUPONS.get(codigo.trim().toUpperCase());
        if (valor == null) throw new RegraNegocioException("Cupom inválido ou expirado.");
        return valor;
    }
}
