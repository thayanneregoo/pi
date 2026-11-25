package br.com.deliveryfacil.model;

import br.com.deliveryfacil.model.enums.StatusProduto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PRODUTO")
@Getter @Setter @NoArgsConstructor
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Long idProduto;

    @Column(nullable = false, length = 100) private String nome;
    @Column(columnDefinition = "varchar(max)") private String descricao;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal preco;
    @Column(nullable = false, length = 50) private String categoria;
    @Column(name = "foto_url", length = 255) private String fotoUrl;

    @Convert(converter = StatusProduto.Conv.class)
    @Column(nullable = false, length = 10)
    private StatusProduto status = StatusProduto.ATIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_funcionario_alteracao")
    private Funcionario funcionarioAlteracao;

    @Column(name = "data_alteracao")
    private LocalDateTime dataAlteracao;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GrupoOpcao> grupos = new ArrayList<>();
}
