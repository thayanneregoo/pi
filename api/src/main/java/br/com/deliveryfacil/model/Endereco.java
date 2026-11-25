package br.com.deliveryfacil.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ENDERECO")
@Getter @Setter @NoArgsConstructor
public class Endereco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_endereco")
    private Long idEndereco;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, length = 150) private String rua;
    @Column(nullable = false, length = 10) private String numero;
    @Column(length = 100) private String complemento;
    @Column(nullable = false, length = 100) private String bairro;
    @Column(nullable = false, length = 9) private String cep;
    @Column(length = 150) private String referencia;
    @Column(nullable = false, precision = 9, scale = 6) private BigDecimal latitude;
    @Column(nullable = false, precision = 9, scale = 6) private BigDecimal longitude;

    @Column(name = "disponivel_entrega", nullable = false)
    private boolean disponivelEntrega = true;
}
