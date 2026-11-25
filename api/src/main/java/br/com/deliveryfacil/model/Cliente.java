package br.com.deliveryfacil.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "CLIENTE")
@Getter @Setter @NoArgsConstructor
public class Cliente extends Pessoa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(nullable = false, unique = true, length = 15)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(name = "telefone_verificado", nullable = false)
    private boolean telefoneVerificado;

    @Override
    public String descricaoPapel() { return "CLIENTE"; }
}
