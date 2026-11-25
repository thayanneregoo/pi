package br.com.deliveryfacil.model;

import br.com.deliveryfacil.model.enums.PapelFuncionario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "FUNCIONARIO")
@Getter @Setter @NoArgsConstructor
public class Funcionario extends Pessoa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_funcionario")
    private Long idFuncionario;

    @Convert(converter = PapelFuncionario.Conv.class)
    @Column(nullable = false, length = 10)
    private PapelFuncionario papel;

    @Override
    public String descricaoPapel() { return papel == null ? "FUNCIONARIO" : papel.name(); }
}
