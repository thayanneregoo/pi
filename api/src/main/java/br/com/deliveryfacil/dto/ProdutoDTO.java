package br.com.deliveryfacil.dto;

import br.com.deliveryfacil.model.GrupoOpcao;
import br.com.deliveryfacil.model.Opcao;
import br.com.deliveryfacil.model.Produto;
import br.com.deliveryfacil.model.enums.StatusProduto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public final class ProdutoDTO {
    private ProdutoDTO() {}

    public record Dados(
            @NotBlank @Size(max = 100) String nome,
            String descricao,
            @NotNull @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero") BigDecimal preco,
            @NotBlank @Size(max = 50) String categoria,
            @Size(max = 255) String fotoUrl) {}

    public record AlterarStatus(@NotNull StatusProduto status) {}

    public record Resposta(Long id, String nome, String descricao, BigDecimal preco, String categoria,
                           String fotoUrl, StatusProduto status) {
        public static Resposta de(Produto p) {
            return new Resposta(p.getIdProduto(), p.getNome(), p.getDescricao(), p.getPreco(),
                    p.getCategoria(), p.getFotoUrl(), p.getStatus());
        }
    }

    public record OpcaoDados(@NotBlank @Size(max = 50) String nome,
                             @DecimalMin("0.00") BigDecimal precoAdicional) {}

    public record GrupoDados(
            @NotBlank @Size(max = 50) String nome,
            boolean obrigatorio,
            @Min(0) int minSelecoes,
            @Min(1) int maxSelecoes,
            @NotEmpty @Valid List<OpcaoDados> opcoes) {}

    public record OpcaoResposta(Long id, String nome, BigDecimal precoAdicional) {
        public static OpcaoResposta de(Opcao o) {
            return new OpcaoResposta(o.getIdOpcao(), o.getNome(), o.getPrecoAdicional());
        }
    }

    public record GrupoResposta(Long id, Long idProduto, String nome, boolean obrigatorio, int minSelecoes,
                                int maxSelecoes, List<OpcaoResposta> opcoes) {
        public static GrupoResposta de(GrupoOpcao g) {
            return new GrupoResposta(g.getIdGrupoOpcao(), g.getProduto().getIdProduto(), g.getNome(),
                    g.isObrigatorio(), g.getMinSelecoes(), g.getMaxSelecoes(),
                    g.getOpcoes().stream().map(OpcaoResposta::de).toList());
        }
    }
}
