package br.com.deliveryfacil.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "AREA_ENTREGA")
@Getter @Setter @NoArgsConstructor
public class AreaEntrega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_area")
    private Long idArea;

    @Column(nullable = false, unique = true, length = 100)
    private String bairro;

    @Column(name = "taxa_entrega", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaEntrega;

    @Column(name = "pedido_minimo", nullable = false, precision = 10, scale = 2)
    private BigDecimal pedidoMinimo = BigDecimal.ZERO;
}
