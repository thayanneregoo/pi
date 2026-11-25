package br.com.deliveryfacil.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/** Classe abstrata com o que é comum a quem se relaciona com o sistema (Cliente e Funcionario). */
@MappedSuperclass
@Getter
@Setter
public abstract class Pessoa {

    @Column(nullable = false, length = 100)
    protected String nome;

    public abstract String descricaoPapel();
}
