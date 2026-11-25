package br.com.deliveryfacil.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "AVALIACAO")
@Getter @Setter @NoArgsConstructor
public class Avaliacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avaliacao")
    private Long idAvaliacao;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @Column(nullable = false) private int nota;
    @Column(columnDefinition = "varchar(max)") private String comentario;

    @Column(name = "resposta_restaurante", columnDefinition = "varchar(max)")
    private String respostaRestaurante;

    @Column(name = "data_avaliacao", nullable = false, updatable = false)
    private LocalDateTime dataAvaliacao;

    @PrePersist
    void antesDePersistir() {
        if (dataAvaliacao == null) dataAvaliacao = LocalDateTime.now();
    }
}
