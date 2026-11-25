package br.com.deliveryfacil.model;

import br.com.deliveryfacil.model.enums.StatusPedido;
import br.com.deliveryfacil.model.enums.TipoEntrega;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PEDIDO")
@Getter @Setter @NoArgsConstructor
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_endereco")
    private Endereco endereco;

    @Convert(converter = TipoEntrega.Conv.class)
    @Column(name = "tipo_entrega", nullable = false, length = 10)
    private TipoEntrega tipoEntrega;

    @Column(name = "data_pedido", nullable = false, updatable = false)
    private LocalDateTime dataPedido;

    @Convert(converter = StatusPedido.Conv.class)
    @Column(nullable = false, length = 30)
    private StatusPedido status = StatusPedido.PENDENTE;

    @Column(name = "subtotal_produtos", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalProdutos = BigDecimal.ZERO;

    @Column(name = "taxa_entrega", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaEntrega = BigDecimal.ZERO;

    @Column(name = "desconto_cupom", nullable = false, precision = 10, scale = 2)
    private BigDecimal descontoCupom = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "motivo_cancelamento", length = 200)
    private String motivoCancelamento;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @PrePersist
    void antesDePersistir() {
        if (dataPedido == null) dataPedido = LocalDateTime.now();
    }

    /** valor_total = subtotal_produtos + taxa_entrega - desconto_cupom (nunca negativo). */
    public void recalcularTotais() {
        subtotalProdutos = itens.stream().map(ItemPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        valorTotal = subtotalProdutos.add(taxaEntrega).subtract(descontoCupom).max(BigDecimal.ZERO);
    }
}
