package br.com.deliveryfacil.service;

import br.com.deliveryfacil.model.enums.StatusPedido;
import br.com.deliveryfacil.repository.HistoricoStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Alerta o operador quando um pedido ultrapassa o SLA de preparo (padrão: 30 min em "Em preparo"). */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlaPreparoJob {
    private final HistoricoStatusRepository historicos;

    @Value("${delivery.sla-preparo-minutos:30}")
    private int slaMinutos;

    @Scheduled(fixedDelayString = "${delivery.sla.varredura-ms:60000}")
    @Transactional(readOnly = true)
    public void alertarAtrasos() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(slaMinutos);
        historicos.findByStatusNovoAndDataAlteracaoBefore(StatusPedido.EM_PREPARO, limite).stream()
                .filter(h -> h.getPedido().getStatus() == StatusPedido.EM_PREPARO)
                .forEach(h -> log.warn("[ALERTA SLA] Pedido {} em preparo há mais de {} minutos.",
                        h.getPedido().getIdPedido(), slaMinutos));
    }
}
