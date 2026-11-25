package br.com.deliveryfacil.dto;

import br.com.deliveryfacil.model.Cliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ClienteDTO {
    private ClienteDTO() {}

    public record Criar(
            @NotBlank @Size(max = 100) String nome,
            @NotBlank @Pattern(regexp = "\\d{10,11}", message = "Telefone deve conter apenas números, com DDD") String telefone,
            @Size(max = 150) @Pattern(regexp = ".*@.*", message = "E-mail deve conter '@'") String email) {}

    public record Atualizar(
            @NotBlank @Size(max = 100) String nome,
            @Size(max = 150) @Pattern(regexp = ".*@.*", message = "E-mail deve conter '@'") String email) {}

    public record VerificarOtp(@NotBlank String codigo) {}

    public record Resposta(Long id, String nome, String telefone, String email, boolean telefoneVerificado) {
        public static Resposta de(Cliente c) {
            return new Resposta(c.getIdCliente(), c.getNome(), c.getTelefone(), c.getEmail(), c.isTelefoneVerificado());
        }
    }
}
