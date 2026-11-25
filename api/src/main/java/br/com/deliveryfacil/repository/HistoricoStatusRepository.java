package br.com.deliveryfacil.repository;

import br.com.deliveryfacil.model.*;
import br.com.deliveryfacil.model.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {
    List<HistoricoStatus> findByPedido_IdPedidoOrderByDataAlteracaoAsc(Long idPedido);
    List<HistoricoStatus> findByStatusNovoAndDataAlteracaoBefore(StatusPedido status, LocalDateTime limite);
}
