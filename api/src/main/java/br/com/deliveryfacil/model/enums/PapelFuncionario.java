package br.com.deliveryfacil.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

public enum PapelFuncionario implements EnumRotulo {
    OPERADOR("operador"), GESTOR("gestor");

    private final String rotulo;

    PapelFuncionario(String rotulo) { this.rotulo = rotulo; }

    @Override
    @JsonValue
    public String getRotulo() { return rotulo; }

    @Converter
    public static class Conv extends ConversorRotulo<PapelFuncionario> {
        public Conv() { super(PapelFuncionario.class); }
    }
}
