package br.com.deliveryfacil.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

public enum StatusPedido implements EnumRotulo {
    PENDENTE("Pendente"), PAGAMENTO_APROVADO("Pagamento aprovado"), ACEITO("Aceito"), EM_PREPARO("Em preparo"), PRONTO("Pronto/Saiu para entrega"), ENTREGUE("Entregue"), CANCELADO("Cancelado");

    private final String rotulo;

    StatusPedido(String rotulo) { this.rotulo = rotulo; }

    @Override
    @JsonValue
    public String getRotulo() { return rotulo; }

    /** Próximo status do fluxo normal: Pagamento aprovado -> Aceito -> Em preparo -> Pronto -> Entregue. */
    public StatusPedido proximo() {
        return switch (this) {
            case PENDENTE -> PAGAMENTO_APROVADO;
            case PAGAMENTO_APROVADO -> ACEITO;
            case ACEITO -> EM_PREPARO;
            case EM_PREPARO -> PRONTO;
            case PRONTO -> ENTREGUE;
            default -> null;
        };
    }

    public boolean podeCancelar() { return this != ENTREGUE && this != CANCELADO; }

    @Converter
    public static class Conv extends ConversorRotulo<StatusPedido> {
        public Conv() { super(StatusPedido.class); }
    }
}
