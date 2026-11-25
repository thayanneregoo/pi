package br.com.deliveryfacil.dto;

import br.com.deliveryfacil.model.Funcionario;
import br.com.deliveryfacil.model.enums.PapelFuncionario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class FuncionarioDTO {
    private FuncionarioDTO() {}

    public record Criar(@NotBlank @Size(max = 100) String nome, @NotNull PapelFuncionario papel) {}

    public record Resposta(Long id, String nome, PapelFuncionario papel) {
        public static Resposta de(Funcionario f) {
            return new Resposta(f.getIdFuncionario(), f.getNome(), f.getPapel());
        }
    }
}
