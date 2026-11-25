package br.com.deliveryfacil.service;

import br.com.deliveryfacil.exception.AcessoNegadoException;
import br.com.deliveryfacil.exception.NaoAutenticadoException;
import br.com.deliveryfacil.model.Funcionario;
import br.com.deliveryfacil.model.enums.PapelFuncionario;
import br.com.deliveryfacil.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Controle de papéis simplificado (cabeçalho X-Funcionario-Id). Em produção seria
 * substituído por Spring Security + JWT, mantendo esta mesma interface de uso.
 */
@Service
@RequiredArgsConstructor
public class AutorizacaoService {
    private final FuncionarioRepository funcionarios;

    public Funcionario exigirFuncionario(Long idFuncionario) {
        if (idFuncionario == null) throw new NaoAutenticadoException("Cabeçalho X-Funcionario-Id é obrigatório.");
        return funcionarios.findById(idFuncionario)
                .orElseThrow(() -> new NaoAutenticadoException("Funcionário não identificado."));
    }

    public Funcionario exigirGestor(Long idFuncionario) {
        Funcionario f = exigirFuncionario(idFuncionario);
        if (f.getPapel() != PapelFuncionario.GESTOR) {
            throw new AcessoNegadoException("Apenas o papel 'gestor' pode executar esta ação.");
        }
        return f;
    }
}
