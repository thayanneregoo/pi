package br.com.deliveryfacil.model;

import br.com.deliveryfacil.model.enums.FormaPagamento;
import br.com.deliveryfacil.model.enums.StatusPagamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PAGAMENTO")
@Getter @Setter @NoArgsConstructor
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pagamento")
    private Long idPagamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Convert(converter = FormaPagamento.Conv.class)
    @Column(name = "forma_pagamento", nullable = false, length = 10)
    private FormaPagamento forma;

    @Convert(converter = StatusPagamento.Conv.class)
    @Column(name = "status_pagamento", nullable = false, length = 10)
    private StatusPagamento status = StatusPagamento.AGUARDANDO;

    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal valor;

    /** Apenas a referência ao gateway: nenhum dado de cartão é guardado. */
    @Column(name = "id_transacao_gateway", length = 100)
    private String idTransacaoGateway;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    void antesDePersistir() {
        if (dataCriacao == null) dataCriacao = LocalDateTime.now();
    }
}
