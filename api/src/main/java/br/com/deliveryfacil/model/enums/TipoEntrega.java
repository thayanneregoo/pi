package br.com.deliveryfacil.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

public enum TipoEntrega implements EnumRotulo {
    ENTREGA("entrega"), RETIRADA("retirada");

    private final String rotulo;

    TipoEntrega(String rotulo) { this.rotulo = rotulo; }

    @Override
    @JsonValue
    public String getRotulo() { return rotulo; }

    @Converter
    public static class Conv extends ConversorRotulo<TipoEntrega> {
        public Conv() { super(TipoEntrega.class); }
    }
}
