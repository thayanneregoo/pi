package br.com.deliveryfacil.dto;

import br.com.deliveryfacil.model.Endereco;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class EnderecoDTO {
    private EnderecoDTO() {}

    public record Dados(
            @NotBlank @Size(max = 150) String rua,
            @NotBlank @Size(max = 10) String numero,
            @Size(max = 100) String complemento,
            @NotBlank @Size(max = 100) String bairro,
            @NotBlank @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido (use 00000-000)") String cep,
            @Size(max = 150) String referencia,
            @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
            @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude) {}

    public record Resposta(Long id, Long idCliente, String rua, String numero, String complemento, String bairro,
                           String cep, String referencia, BigDecimal latitude, BigDecimal longitude,
                           boolean disponivelEntrega) {
        public static Resposta de(Endereco e) {
            return new Resposta(e.getIdEndereco(), e.getCliente().getIdCliente(), e.getRua(), e.getNumero(),
                    e.getComplemento(), e.getBairro(), e.getCep(), e.getReferencia(), e.getLatitude(),
                    e.getLongitude(), e.isDisponivelEntrega());
        }
    }
}
