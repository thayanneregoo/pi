package br.com.deliveryfacil.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public final class AreaEntregaDTO {
    private AreaEntregaDTO() {}

    public record Dados(
            @NotBlank @Size(max = 100) String bairro,
            @NotNull @DecimalMin("0.00") BigDecimal taxaEntrega,
            @NotNull @DecimalMin("0.00") BigDecimal pedidoMinimo) {}
}
