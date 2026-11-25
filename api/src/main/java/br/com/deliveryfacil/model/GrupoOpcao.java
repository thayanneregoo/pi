package br.com.deliveryfacil.model;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GRUPO_OPCAO")
@Getter @Setter @NoArgsConstructor
public class GrupoOpcao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo_opcao")
    private Long idGrupoOpcao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @Column(nullable = false, length = 50) private String nome;
    @Column(nullable = false) private boolean obrigatorio;
    @Column(name = "min_selecoes", nullable = false) private int minSelecoes;
    @Column(name = "max_selecoes", nullable = false) private int maxSelecoes = 1;

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Opcao> opcoes = new ArrayList<>();
}
