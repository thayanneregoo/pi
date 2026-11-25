package br.com.deliveryfacil.service;

import br.com.deliveryfacil.dto.AvaliacaoDTO;
import br.com.deliveryfacil.exception.ConflitoException;
import br.com.deliveryfacil.exception.RecursoNaoEncontradoException;
import br.com.deliveryfacil.exception.RegraNegocioException;
import br.com.deliveryfacil.model.Avaliacao;
import br.com.deliveryfacil.model.Pedido;
import br.com.deliveryfacil.model.enums.StatusPedido;
import br.com.deliveryfacil.repository.AvaliacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AvaliacaoService {
    private final AvaliacaoRepository avaliacoes;
    private final PedidoService pedidoService;
    private final AutorizacaoService autorizacao;

    public Avaliacao criar(Long idPedido, AvaliacaoDTO.Criar d) {
        Pedido p = pedidoService.buscar(idPedido);
        if (p.getStatus() != StatusPedido.ENTREGUE) {
            throw new RegraNegocioException("Só é possível avaliar pedidos com status Entregue.");
        }
        if (avaliacoes.existsByPedido_IdPedido(idPedido)) {
            throw new ConflitoException("Este pedido já foi avaliado.");
        }
        Avaliacao a = new Avaliacao();
        a.setPedido(p);
        a.setNota(d.nota());
        a.setComentario(d.comentario());
        return avaliacoes.save(a);
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listar() { return avaliacoes.findAllByOrderByDataAvaliacaoDesc(); }

    /** Resposta pública do restaurante (qualquer funcionário identificado). */
    public Avaliacao responder(Long idAvaliacao, String resposta, Long idFuncionario) {
        autorizacao.exigirFuncionario(idFuncionario);
        Avaliacao a = avaliacoes.findById(idAvaliacao)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação não encontrada: " + idAvaliacao));
        a.setRespostaRestaurante(resposta);
        return avaliacoes.save(a);
    }
}
