package br.com.deliveryfacil.service;

import br.com.deliveryfacil.model.Pedido;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Ponto único de notificações. Aqui apenas registra em log; integra-se depois a push/WhatsApp. */
@Slf4j
@Service
public class NotificacaoService {
    public void notificarStatus(Pedido pedido) {
        log.info("[NOTIFICAÇÃO] Cliente {} - pedido {} agora está '{}'.",
                pedido.getCliente().getIdCliente(), pedido.getIdPedido(), pedido.getStatus().getRotulo());
    }

    public void notificarPagamentoRecusado(Pedido pedido) {
        log.info("[NOTIFICAÇÃO] Cliente {} - pagamento do pedido {} recusado; nova tentativa permitida.",
                pedido.getCliente().getIdCliente(), pedido.getIdPedido());
    }
}
