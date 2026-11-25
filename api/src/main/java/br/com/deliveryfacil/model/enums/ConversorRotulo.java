package br.com.deliveryfacil.model.enums;

import jakarta.persistence.AttributeConverter;

/** Classe abstrata reaproveitada por todos os conversores enum <-> coluna VARCHAR. */
public abstract class ConversorRotulo<E extends Enum<E> & EnumRotulo> implements AttributeConverter<E, String> {
    private final Class<E> tipo;

    protected ConversorRotulo(Class<E> tipo) {
        this.tipo = tipo;
    }

    @Override
    public String convertToDatabaseColumn(E atributo) {
        return atributo == null ? null : atributo.getRotulo();
    }

    @Override
    public E convertToEntityAttribute(String dado) {
        return dado == null ? null : EnumRotulo.deRotulo(tipo, dado);
    }
}
