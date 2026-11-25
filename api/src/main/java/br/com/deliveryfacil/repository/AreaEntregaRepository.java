package br.com.deliveryfacil.repository;

import br.com.deliveryfacil.model.*;
import br.com.deliveryfacil.model.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AreaEntregaRepository extends JpaRepository<AreaEntrega, Long> {
    Optional<AreaEntrega> findByBairroIgnoreCase(String bairro);
    boolean existsByBairroIgnoreCase(String bairro);
}
