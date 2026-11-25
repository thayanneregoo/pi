package br.com.deliveryfacil.repository;

import br.com.deliveryfacil.model.*;
import br.com.deliveryfacil.model.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findAllByOrderByDataPedidoDesc();
    List<Pedido> findByStatusOrderByDataPedidoDesc(StatusPedido status);
    List<Pedido> findByCliente_IdClienteOrderByDataPedidoDesc(Long idCliente);
    List<Pedido> findByStatusAndCliente_IdClienteOrderByDataPedidoDesc(StatusPedido status, Long idCliente);
}
