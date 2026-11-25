package br.com.deliveryfacil.service;

import br.com.deliveryfacil.dto.ProdutoDTO;
import br.com.deliveryfacil.exception.RecursoNaoEncontradoException;
import br.com.deliveryfacil.exception.RegraNegocioException;
import br.com.deliveryfacil.model.Funcionario;
import br.com.deliveryfacil.model.GrupoOpcao;
import br.com.deliveryfacil.model.Opcao;
import br.com.deliveryfacil.model.Produto;
import br.com.deliveryfacil.model.enums.StatusProduto;
import br.com.deliveryfacil.repository.GrupoOpcaoRepository;
import br.com.deliveryfacil.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Só o papel "gestor" cria/edita/pausa produtos. O registro de quem alterou preço/status
 * é gravado em PRODUTO e replicado em HISTORICO_PRODUTO pelo trigger trg_produto_auditoria.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProdutoService {
    private final ProdutoRepository produtos;
    private final GrupoOpcaoRepository grupos;
    private final AutorizacaoService autorizacao;

    public Produto criar(ProdutoDTO.Dados d, Long idFuncionario) {
        Funcionario gestor = autorizacao.exigirGestor(idFuncionario);
        Produto p = new Produto();
        preencher(p, d);
        p.setStatus(StatusProduto.ATIVO);
        p.setFuncionarioAlteracao(gestor);
        p.setDataAlteracao(LocalDateTime.now());
        return produtos.save(p);
    }

    public Produto atualizar(Long id, ProdutoDTO.Dados d, Long idFuncionario) {
        Funcionario gestor = autorizacao.exigirGestor(idFuncionario);
        Produto p = buscar(id);
        preencher(p, d);
        p.setFuncionarioAlteracao(gestor);
        p.setDataAlteracao(LocalDateTime.now());
        return produtos.save(p);
    }

    public Produto alterarStatus(Long id, StatusProduto status, Long idFuncionario) {
        Funcionario gestor = autorizacao.exigirGestor(idFuncionario);
        Produto p = buscar(id);
        p.setStatus(status);
        p.setFuncionarioAlteracao(gestor);
        p.setDataAlteracao(LocalDateTime.now());
        return produtos.save(p);
    }

    @Transactional(readOnly = true)
    public Produto buscar(Long id) {
        return produtos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Produto> listar(String categoria, StatusProduto status) {
        boolean temCategoria = categoria != null && !categoria.isBlank();
        if (temCategoria && status != null) return produtos.findByCategoriaIgnoreCaseAndStatus(categoria, status);
        if (temCategoria) return produtos.findByCategoriaIgnoreCase(categoria);
        if (status != null) return produtos.findByStatus(status);
        return produtos.findAll();
    }

    /* ----- grupos de opção (variações e adicionais) ----- */

    public GrupoOpcao criarGrupo(Long idProduto, ProdutoDTO.GrupoDados d, Long idFuncionario) {
        autorizacao.exigirGestor(idFuncionario);
        Produto p = buscar(idProduto);
        if (d.minSelecoes() > d.maxSelecoes()) throw new RegraNegocioException("min_selecoes não pode ser maior que max_selecoes.");
        if (d.obrigatorio() && d.minSelecoes() < 1) throw new RegraNegocioException("Grupo obrigatório exige min_selecoes >= 1.");
        if (d.maxSelecoes() > d.opcoes().size()) throw new RegraNegocioException("max_selecoes maior que o número de opções do grupo.");

        GrupoOpcao g = new GrupoOpcao();
        g.setProduto(p);
        g.setNome(d.nome().trim());
        g.setObrigatorio(d.obrigatorio());
        g.setMinSelecoes(d.minSelecoes());
        g.setMaxSelecoes(d.maxSelecoes());
        for (ProdutoDTO.OpcaoDados od : d.opcoes()) {
            Opcao o = new Opcao();
            o.setGrupo(g);
            o.setNome(od.nome().trim());
            o.setPrecoAdicional(od.precoAdicional() == null ? BigDecimal.ZERO : od.precoAdicional());
            g.getOpcoes().add(o);
        }
        return grupos.save(g);
    }

    @Transactional(readOnly = true)
    public List<GrupoOpcao> listarGrupos(Long idProduto) {
        buscar(idProduto);
        return grupos.findByProduto_IdProduto(idProduto);
    }

    public void excluirGrupo(Long idGrupo, Long idFuncionario) {
        autorizacao.exigirGestor(idFuncionario);
        GrupoOpcao g = grupos.findById(idGrupo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Grupo não encontrado: " + idGrupo));
        grupos.delete(g);
        grupos.flush();
    }

    private void preencher(Produto p, ProdutoDTO.Dados d) {
        p.setNome(d.nome().trim());
        p.setDescricao(d.descricao());
        p.setPreco(d.preco());
        p.setCategoria(d.categoria().trim());
        p.setFotoUrl(d.fotoUrl());
    }
}
