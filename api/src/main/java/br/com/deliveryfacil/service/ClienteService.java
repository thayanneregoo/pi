package br.com.deliveryfacil.service;

import br.com.deliveryfacil.dto.ClienteDTO;
import br.com.deliveryfacil.exception.ConflitoException;
import br.com.deliveryfacil.exception.RecursoNaoEncontradoException;
import br.com.deliveryfacil.model.Cliente;
import br.com.deliveryfacil.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteService {
    private final ClienteRepository clientes;

    public Cliente criar(ClienteDTO.Criar dados) {
        if (clientes.existsByTelefone(dados.telefone())) {
            throw new ConflitoException("Já existe cliente cadastrado com este telefone.");
        }
        Cliente c = new Cliente();
        c.setNome(dados.nome().trim());
        c.setTelefone(dados.telefone());
        c.setEmail(dados.email());
        return clientes.save(c);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() { return clientes.findAll(); }

    @Transactional(readOnly = true)
    public Cliente buscar(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + id));
    }

    public Cliente atualizar(Long id, ClienteDTO.Atualizar dados) {
        Cliente c = buscar(id);
        c.setNome(dados.nome().trim());
        c.setEmail(dados.email());
        return clientes.save(c);
    }

    /** Exclusão a pedido do titular (LGPD). Clientes com pedidos retornam 409 e devem ser anonimizados. */
    public void excluir(Long id) {
        clientes.delete(buscar(id));
        clientes.flush();
    }
}
