package br.com.deliveryfacil.service;

import br.com.deliveryfacil.dto.FuncionarioDTO;
import br.com.deliveryfacil.model.Funcionario;
import br.com.deliveryfacil.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FuncionarioService {
    private final FuncionarioRepository funcionarios;

    public Funcionario criar(FuncionarioDTO.Criar d) {
        Funcionario f = new Funcionario();
        f.setNome(d.nome().trim());
        f.setPapel(d.papel());
        return funcionarios.save(f);
    }

    @Transactional(readOnly = true)
    public List<Funcionario> listar() { return funcionarios.findAll(); }
}
