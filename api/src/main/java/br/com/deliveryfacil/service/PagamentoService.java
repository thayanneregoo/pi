package br.com.deliveryfacil.service;

import br.com.deliveryfacil.dto.PagamentoDTO;
import br.com.deliveryfacil.exception.ConflitoException;
import br.com.deliveryfacil.exception.RecursoNaoEncontradoException;
import br.com.deliveryfacil.exception.RegraNegocioException;
import br.com.deliveryfacil.model.AreaEntrega;
import br.com.deliveryfacil.model.Pagamento;
import br.com.deliveryfacil.model.Pedido;
import br.com.deliveryfacil.model.enums.FormaPagamento;
import br.com.deliveryfacil.model.enums.StatusPagamento;
import br.com.deliveryfacil.model.enums.StatusPedido;
import br.com.deliveryfacil.model.enums.TipoEntrega;
import br.com.deliveryfacil.repository.AreaEntregaRepository;
import br.com.deliveryfacil.repository.PagamentoRepository;
import br.com.deliveryfacil.service.pagamento.ProcessadorPagamento;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class PagamentoService {
    private final PagamentoRepository pagamentos;
    private final AreaEntregaRepository areas;
    private final PedidoService pedidoService;
    private final NotificacaoService notificacoes;
    private final Map<FormaPagamento, ProcessadorPagamento> processadores = new EnumMap<>(FormaPagamento.class);

    public PagamentoService(PagamentoRepository pagamentos, AreaEntregaRepository areas, PedidoService pedidoService,
                            NotificacaoService notificacoes, List<ProcessadorPagamento> lista) {
        this.pagamentos = pagamentos;
        this.areas = areas;
        this.pedidoService = pedidoService;
        this.notificacoes = notificacoes;
        lista.forEach(p -> processadores.put(p.forma(), p));   // polimorfismo: uma estratégia por forma
    }

    public Pagamento iniciar(Long idPedido, FormaPagamento forma) {
        Pedido p = pedidoService.buscar(idPedido);
        if (p.getStatus() != StatusPedido.PENDENTE) {
            throw new RegraNegocioException("Somente pedidos Pendentes aceitam pagamento.");
        }
        if (p.getItens().isEmpty()) throw new RegraNegocioException("O pedido não possui itens.");
        if (p.getTipoEntrega() == TipoEntrega.ENTREGA) {
            AreaEntrega area = areas.findByBairroIgnoreCase(p.getEndereco().getBairro())
                    .orElseThrow(() -> new RegraNegocioException("Bairro sem cobertura de entrega."));
            if (p.getSubtotalProdutos().compareTo(area.getPedidoMinimo()) < 0) {
                throw new RegraNegocioException("Pedido mínimo para " + area.getBairro() + ": R$ " + area.getPedidoMinimo());
            }
        }
        boolean emAndamento = pagamentos.findByPedido_IdPedido(idPedido).stream()
                .anyMatch(x -> x.getStatus() == StatusPagamento.AGUARDANDO || x.getStatus() == StatusPagamento.APROVADO);
        if (emAndamento) throw new ConflitoException("Já existe pagamento em andamento ou aprovado para este pedido.");

        ProcessadorPagamento processador = processadores.get(forma);
        if (processador == null) throw new RegraNegocioException("Forma de pagamento não suportada.");
        return pagamentos.save(processador.criar(p));
    }

    @Transactional(readOnly = true)
    public List<Pagamento> listarDoPedido(Long idPedido) {
        pedidoService.buscar(idPedido);
        return pagamentos.findByPedido_IdPedido(idPedido);
    }

    /** Webhook do gateway, idempotente: repetir o mesmo status não altera nada e responde 200. */
    public Pagamento processarWebhook(PagamentoDTO.Webhook w) {
        Pagamento pg = pagamentos.findByIdTransacaoGateway(w.idTransacaoGateway())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação desconhecida: " + w.idTransacaoGateway()));
        if (pg.getStatus() == w.status()) return pg;

        boolean transicaoValida = pg.getStatus() == StatusPagamento.AGUARDANDO
                || (pg.getStatus() == StatusPagamento.APROVADO && w.status() == StatusPagamento.ESTORNADO);
        if (!transicaoValida) {
            throw new ConflitoException("Transição de pagamento inválida: " + pg.getStatus().getRotulo() + " -> " + w.status().getRotulo());
        }
        pg.setStatus(w.status());
        if (w.status() == StatusPagamento.APROVADO) {
            pedidoService.registrarPagamentoAprovado(pg.getPedido());
        } else if (w.status() == StatusPagamento.RECUSADO) {
            notificacoes.notificarPagamentoRecusado(pg.getPedido());
        }
        return pagamentos.save(pg);
    }

    /** Pix pendente por mais de 15 minutos cancela o pedido automaticamente. */
    @Scheduled(fixedDelayString = "${delivery.pix.varredura-ms:60000}")
    public void cancelarPixExpirados() {
        ProcessadorPagamento pix = processadores.get(FormaPagamento.PIX);
        LocalDateTime limite = LocalDateTime.now().minus(pix.prazoExpiracao());
        List<Pagamento> expirados = pagamentos.findByFormaAndStatusAndDataCriacaoBefore(
                FormaPagamento.PIX, StatusPagamento.AGUARDANDO, limite);
        for (Pagamento pg : expirados) {
            pg.setStatus(StatusPagamento.RECUSADO);
            pedidoService.cancelarPorSistema(pg.getPedido(), "Pix não pago em 15 minutos");
            log.info("Pix {} expirado; pedido {} cancelado.", pg.getIdTransacaoGateway(), pg.getPedido().getIdPedido());
        }
    }
}
