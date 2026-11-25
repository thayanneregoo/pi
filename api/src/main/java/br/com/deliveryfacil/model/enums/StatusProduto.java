package br.com.deliveryfacil.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

public enum StatusProduto implements EnumRotulo {
    ATIVO("ativo"), PAUSADO("pausado");

    private final String rotulo;

    StatusProduto(String rotulo) { this.rotulo = rotulo; }

    @Override
    @JsonValue
    public String getRotulo() { return rotulo; }

    @Converter
    public static class Conv extends ConversorRotulo<StatusProduto> {
        public Conv() { super(StatusProduto.class); }
    }
}
