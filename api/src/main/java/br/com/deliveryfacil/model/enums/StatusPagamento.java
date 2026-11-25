package br.com.deliveryfacil.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

public enum StatusPagamento implements EnumRotulo {
    AGUARDANDO("aguardando"), APROVADO("aprovado"), RECUSADO("recusado"), ESTORNADO("estornado");

    private final String rotulo;

    StatusPagamento(String rotulo) { this.rotulo = rotulo; }

    @Override
    @JsonValue
    public String getRotulo() { return rotulo; }

    @Converter
    public static class Conv extends ConversorRotulo<StatusPagamento> {
        public Conv() { super(StatusPagamento.class); }
    }
}
