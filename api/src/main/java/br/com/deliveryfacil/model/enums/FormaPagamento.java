package br.com.deliveryfacil.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

public enum FormaPagamento implements EnumRotulo {
    PIX("pix"), CARTAO("cartao");

    private final String rotulo;

    FormaPagamento(String rotulo) { this.rotulo = rotulo; }

    @Override
    @JsonValue
    public String getRotulo() { return rotulo; }

    @Converter
    public static class Conv extends ConversorRotulo<FormaPagamento> {
        public Conv() { super(FormaPagamento.class); }
    }
}
