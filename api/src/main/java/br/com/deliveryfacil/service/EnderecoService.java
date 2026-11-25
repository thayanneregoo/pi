package br.com.deliveryfacil.service;

import br.com.deliveryfacil.dto.EnderecoDTO;
import br.com.deliveryfacil.exception.RecursoNaoEncontradoException;
import br.com.deliveryfacil.model.Cliente;
import br.com.deliveryfacil.model.Endereco;
import br.com.deliveryfacil.repository.AreaEntregaRepository;
import br.com.deliveryfacil.repository.ClienteRepository;
import br.com.deliveryfacil.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EnderecoService {
    private final EnderecoRepository enderecos;
    private final ClienteRepository clientes;
    private final AreaEntregaRepository areas;

    public Endereco criar(Long idCliente, EnderecoDTO.Dados dados) {
        Cliente c = clientes.findById(idCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + idCliente));
        Endereco e = new Endereco();
        e.setCliente(c);
        preencher(e, dados);
        return enderecos.save(e);
    }

    @Transactional(readOnly = true)
    public List<Endereco> listarDoCliente(Long idCliente) {
        return enderecos.findByCliente_IdCliente(idCliente);
    }

    public Endereco atualizar(Long id, EnderecoDTO.Dados dados) {
        Endereco e = buscar(id);
        preencher(e, dados);
        return enderecos.save(e);
    }

    public void excluir(Long id) {
        enderecos.delete(buscar(id));
        enderecos.flush();
    }

    private Endereco buscar(Long id) {
        return enderecos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereço não encontrado: " + id));
    }

    /** Bairro fora da tabela de área de entrega => endereço "indisponível para entrega" (retirada continua permitida). */
    private void preencher(Endereco e, EnderecoDTO.Dados d) {
        e.setRua(d.rua());
        e.setNumero(d.numero());
        e.setComplemento(d.complemento());
        e.setBairro(d.bairro().trim());
        e.setCep(normalizarCep(d.cep()));
        e.setReferencia(d.referencia());
        e.setLatitude(d.latitude());
        e.setLongitude(d.longitude());
        e.setDisponivelEntrega(areas.existsByBairroIgnoreCase(e.getBairro()));
    }

    private String normalizarCep(String cep) {
        String n = cep.replace("-", "");
        return n.substring(0, 5) + "-" + n.substring(5);
    }
}
