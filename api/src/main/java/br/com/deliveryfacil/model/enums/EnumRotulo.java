package br.com.deliveryfacil.model.enums;

/** Contrato dos enums que são gravados no banco por um rótulo textual (ex.: "Em preparo"). */
public interface EnumRotulo {
    String getRotulo();

    static <E extends Enum<E> & EnumRotulo> E deRotulo(Class<E> tipo, String rotulo) {
        for (E e : tipo.getEnumConstants()) {
            if (e.getRotulo().equalsIgnoreCase(rotulo)) return e;
        }
        throw new IllegalArgumentException("Valor inválido: " + rotulo);
    }
}
