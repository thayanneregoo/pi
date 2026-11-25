package br.com.deliveryfacil.service;

import br.com.deliveryfacil.dto.AreaEntregaDTO;
import br.com.deliveryfacil.exception.ConflitoException;
import br.com.deliveryfacil.exception.RecursoNaoEncontradoException;
import br.com.deliveryfacil.model.AreaEntrega;
import br.com.deliveryfacil.repository.AreaEntregaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AreaEntregaService {
    private final AreaEntregaRepository areas;
    private final AutorizacaoService autorizacao;

    public AreaEntrega criar(AreaEntregaDTO.Dados d, Long idFuncionario) {
        autorizacao.exigirGestor(idFuncionario);
        if (areas.existsByBairroIgnoreCase(d.bairro())) throw new ConflitoException("Bairro já cadastrado.");
        AreaEntrega a = new AreaEntrega();
        preencher(a, d);
        return areas.save(a);
    }

    @Transactional(readOnly = true)
    public List<AreaEntrega> listar() { return areas.findAll(); }

    public AreaEntrega atualizar(Long id, AreaEntregaDTO.Dados d, Long idFuncionario) {
        autorizacao.exigirGestor(idFuncionario);
        AreaEntrega a = buscar(id);
        preencher(a, d);
        return areas.save(a);
    }

    public void excluir(Long id, Long idFuncionario) {
        autorizacao.exigirGestor(idFuncionario);
        areas.delete(buscar(id));
    }

    private AreaEntrega buscar(Long id) {
        return areas.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Área não encontrada: " + id));
    }

    private void preencher(AreaEntrega a, AreaEntregaDTO.Dados d) {
        a.setBairro(d.bairro().trim());
        a.setTaxaEntrega(d.taxaEntrega());
        a.setPedidoMinimo(d.pedidoMinimo());
    }
}
