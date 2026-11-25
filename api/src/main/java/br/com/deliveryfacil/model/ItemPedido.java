package br.com.deliveryfacil.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ITEM_PEDIDO")
@Getter @Setter @NoArgsConstructor
public class ItemPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long idItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @Column(nullable = false) private int quantidade;

    /** Preço congelado no momento da adição ao carrinho. */
    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(length = 200) private String observacao;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemOpcao> opcoes = new ArrayList<>();

    /** quantidade x (preço unitário + adicionais das opções). */
    public BigDecimal subtotal() {
        BigDecimal adicionais = opcoes.stream().map(ItemOpcao::getPrecoAdicional).reduce(BigDecimal.ZERO, BigDecimal::add);
        return precoUnitario.add(adicionais).multiply(BigDecimal.valueOf(quantidade));
    }
}
