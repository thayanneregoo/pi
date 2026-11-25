package br.com.deliveryfacil.service;

import br.com.deliveryfacil.dto.PedidoDTO;
import br.com.deliveryfacil.exception.ConflitoException;
import br.com.deliveryfacil.exception.RecursoNaoEncontradoException;
import br.com.deliveryfacil.exception.RegraNegocioException;
import br.com.deliveryfacil.model.*;
import br.com.deliveryfacil.model.enums.EnumRotulo;
import br.com.deliveryfacil.model.enums.StatusPedido;
import br.com.deliveryfacil.model.enums.StatusProduto;
import br.com.deliveryfacil.model.enums.TipoEntrega;
import br.com.deliveryfacil.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Regras de negócio do pedido: carrinho, itens, totais, cupom, fluxo de status e cancelamento. */
@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {
    private final PedidoRepository pedidos;
    private final ClienteRepository clientes;
    private final EnderecoRepository enderecos;
    private final AreaEntregaRepository areas;
    private final ProdutoRepository produtos;
    private final OpcaoRepository opcoes;
    private final HistoricoStatusRepository historicos;
    private final AutorizacaoService autorizacao;
    private final CupomService cupons;
    private final NotificacaoService notificacoes;

    /* ---------- carrinho ---------- */

    public Pedido criar(PedidoDTO.Criar req) {
        Cliente cliente = clientes.findById(req.idCliente())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + req.idCliente()));
        if (!cliente.isTelefoneVerificado()) {
            throw new RegraNegocioException("O telefone do cliente precisa ser verificado por OTP antes do pedido.");
        }
        Pedido p = new Pedido();
        p.setCliente(cliente);
        p.setTipoEntrega(req.tipoEntrega());

        if (req.tipoEntrega() == TipoEntrega.ENTREGA) {
            if (req.idEndereco() == null) throw new RegraNegocioException("Endereço é obrigatório para entrega.");
            Endereco e = enderecos.findById(req.idEndereco())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Endereço não encontrado: " + req.idEndereco()));
            if (!e.getCliente().getIdCliente().equals(cliente.getIdCliente())) {
                throw new RegraNegocioException("O endereço informado não pertence ao cliente.");
            }
            if (!e.isDisponivelEntrega()) {
                throw new RegraNegocioException("Endereço fora da área de cobertura. Escolha retirada ou outro endereço.");
            }
            AreaEntrega area = areas.findByBairroIgnoreCase(e.getBairro())
                    .orElseThrow(() -> new RegraNegocioException("Bairro sem cobertura de entrega."));
            p.setEndereco(e);
            p.setTaxaEntrega(area.getTaxaEntrega());
        } else {
            p.setEndereco(null);
            p.setTaxaEntrega(BigDecimal.ZERO);   // retirada: taxa = 0
        }
        p.recalcularTotais();
        return pedidos.save(p);
    }

    @Transactional(readOnly = true)
    public Pedido buscar(Long id) {
        return pedidos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Pedido> listar(String status, Long idCliente) {
        StatusPedido st = (status == null || status.isBlank()) ? null : EnumRotulo.deRotulo(StatusPedido.class, status);
        if (st != null && idCliente != null) return pedidos.findByStatusAndCliente_IdClienteOrderByDataPedidoDesc(st, idCliente);
        if (st != null) return pedidos.findByStatusOrderByDataPedidoDesc(st);
        if (idCliente != null) return pedidos.findByCliente_IdClienteOrderByDataPedidoDesc(idCliente);
        return pedidos.findAllByOrderByDataPedidoDesc();
    }

    public Pedido adicionarItem(Long idPedido, PedidoDTO.Item req) {
        Pedido p = buscar(idPedido);
        exigirPendente(p);

        Produto produto = produtos.findById(req.idProduto())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + req.idProduto()));
        if (produto.getStatus() != StatusProduto.ATIVO) {
            throw new RegraNegocioException("Produto pausado não pode ser adicionado ao carrinho.");
        }

        Set<Long> ids = req.opcoes() == null ? Set.of() : new HashSet<>(req.opcoes());
        List<Opcao> escolhidas = opcoes.findAllById(ids);
        if (escolhidas.size() != ids.size()) throw new RegraNegocioException("Uma ou mais opções informadas não existem.");
        validarOpcoes(produto, escolhidas);

        ItemPedido item = new ItemPedido();
        item.setPedido(p);
        item.setProduto(produto);
        item.setQuantidade(req.quantidade());
        item.setPrecoUnitario(produto.getPreco());          // preço congelado
        item.setObservacao(req.observacao());
        for (Opcao o : escolhidas) {
            ItemOpcao io = new ItemOpcao();
            io.setItem(item);
            io.setOpcao(o);
            io.setPrecoAdicional(o.getPrecoAdicional());     // adicional congelado
            item.getOpcoes().add(io);
        }
        p.getItens().add(item);
        p.recalcularTotais();
        return pedidos.save(p);
    }

    public Pedido removerItem(Long idPedido, Long idItem) {
        Pedido p = buscar(idPedido);
        exigirPendente(p);
        boolean removido = p.getItens().removeIf(i -> i.getIdItem().equals(idItem));
        if (!removido) throw new RecursoNaoEncontradoException("Item não encontrado no pedido: " + idItem);
        p.recalcularTotais();
        return pedidos.save(p);
    }

    public Pedido aplicarCupom(Long idPedido, String codigo) {
        Pedido p = buscar(idPedido);
        exigirPendente(p);
        p.setDescontoCupom(cupons.valorDesconto(codigo));
        p.recalcularTotais();
        return pedidos.save(p);
    }

    /* ---------- status ---------- */

    /** Ação do restaurante: segue o fluxo permitido ou cancela com motivo obrigatório. */
    public Pedido atualizarStatus(Long idPedido, StatusPedido novo, String motivo, Long idFuncionario) {
        Funcionario f = autorizacao.exigirFuncionario(idFuncionario);
        Pedido p = buscar(idPedido);
        if (novo == StatusPedido.CANCELADO) {
            if (!p.getStatus().podeCancelar()) {
                throw new RegraNegocioException("Pedido " + p.getStatus().getRotulo() + " não pode mais ser cancelado.");
            }
            if (motivo == null || motivo.isBlank()) throw new RegraNegocioException("Motivo é obrigatório para cancelar.");
        } else if (p.getStatus() == StatusPedido.PENDENTE) {
            throw new RegraNegocioException("O pedido só sai de 'Pendente' após o pagamento ser aprovado pelo gateway.");
        } else if (novo != p.getStatus().proximo()) {
            throw new RegraNegocioException("Transição inválida: " + p.getStatus().getRotulo() + " -> " + novo.getRotulo());
        }
        mudarStatus(p, novo, f, motivo);
        return p;
    }

    /** Ação do cliente: só pode cancelar enquanto o pedido está Pendente. */
    public Pedido cancelarPeloCliente(Long idPedido, String motivo) {
        Pedido p = buscar(idPedido);
        if (p.getStatus() != StatusPedido.PENDENTE) {
            throw new RegraNegocioException("O cliente só pode cancelar enquanto o pedido está Pendente.");
        }
        mudarStatus(p, StatusPedido.CANCELADO, null, (motivo == null || motivo.isBlank()) ? "Cancelado pelo cliente" : motivo);
        return p;
    }

    /** Chamado pelo pagamento (webhook): libera o pedido ao restaurante. */
    public void registrarPagamentoAprovado(Pedido p) {
        if (p.getStatus() != StatusPedido.PENDENTE) {
            throw new ConflitoException("Pedido " + p.getIdPedido() + " não está Pendente; pagamento requer conciliação/estorno.");
        }
        mudarStatus(p, StatusPedido.PAGAMENTO_APROVADO, null, null);
    }

    /** Chamado pelo job de expiração do Pix. */
    public void cancelarPorSistema(Pedido p, String motivo) {
        if (p.getStatus() == StatusPedido.PENDENTE) mudarStatus(p, StatusPedido.CANCELADO, null, motivo);
    }

    @Transactional(readOnly = true)
    public List<HistoricoStatus> historico(Long idPedido) {
        buscar(idPedido);
        return historicos.findByPedido_IdPedidoOrderByDataAlteracaoAsc(idPedido);
    }

    /* ---------- internos ---------- */

    private void mudarStatus(Pedido p, StatusPedido novo, Funcionario f, String motivo) {
        StatusPedido anterior = p.getStatus();
        p.setStatus(novo);
        if (novo == StatusPedido.CANCELADO) p.setMotivoCancelamento(motivo);
        pedidos.save(p);

        HistoricoStatus h = new HistoricoStatus();
        h.setPedido(p);
        h.setStatusAnterior(anterior);
        h.setStatusNovo(novo);
        h.setFuncionario(f);
        h.setMotivo(motivo);
        historicos.save(h);
        notificacoes.notificarStatus(p);
    }

    private void exigirPendente(Pedido p) {
        if (p.getStatus() != StatusPedido.PENDENTE) {
            throw new RegraNegocioException("O pedido não pode mais ser alterado (status: " + p.getStatus().getRotulo() + ").");
        }
    }

    /** Opções devem pertencer ao produto; grupo obrigatório exige seleção; respeita min/max por grupo. */
    private void validarOpcoes(Produto produto, List<Opcao> escolhidas) {
        for (Opcao o : escolhidas) {
            if (!o.getGrupo().getProduto().getIdProduto().equals(produto.getIdProduto())) {
                throw new RegraNegocioException("A opção " + o.getIdOpcao() + " não pertence ao produto " + produto.getNome() + ".");
            }
        }
        Map<Long, Long> porGrupo = escolhidas.stream()
                .collect(Collectors.groupingBy(o -> o.getGrupo().getIdGrupoOpcao(), Collectors.counting()));
        for (GrupoOpcao g : produto.getGrupos()) {
            long qtd = porGrupo.getOrDefault(g.getIdGrupoOpcao(), 0L);
            if (qtd == 0 && g.isObrigatorio()) {
                throw new RegraNegocioException("Selecione uma opção do grupo obrigatório '" + g.getNome() + "'.");
            }
            if (qtd > 0 && (qtd < g.getMinSelecoes() || qtd > g.getMaxSelecoes())) {
                throw new RegraNegocioException("Grupo '" + g.getNome() + "': selecione entre "
                        + g.getMinSelecoes() + " e " + g.getMaxSelecoes() + " opções.");
            }
        }
    }
}
