package br.com.deliveryfacil.model;

import br.com.deliveryfacil.model.enums.StatusPedido;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "HISTORICO_STATUS")
@Getter @Setter @NoArgsConstructor
public class HistoricoStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historico_status")
    private Long idHistoricoStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Convert(converter = StatusPedido.Conv.class)
    @Column(name = "status_anterior", nullable = false, length = 30)
    private StatusPedido statusAnterior;

    @Convert(converter = StatusPedido.Conv.class)
    @Column(name = "status_novo", nullable = false, length = 30)
    private StatusPedido statusNovo;

    @Column(name = "data_alteracao", nullable = false)
    private LocalDateTime dataAlteracao;

    /** Nulo quando a mudança foi feita pelo cliente ou pelo sistema. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_funcionario")
    private Funcionario funcionario;

    @Column(length = 200) private String motivo;

    @PrePersist
    void antesDePersistir() {
        if (dataAlteracao == null) dataAlteracao = LocalDateTime.now();
    }
}
