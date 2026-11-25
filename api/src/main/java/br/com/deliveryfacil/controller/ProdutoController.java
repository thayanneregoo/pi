package br.com.deliveryfacil.controller;

import br.com.deliveryfacil.dto.ProdutoDTO;
import br.com.deliveryfacil.model.enums.EnumRotulo;
import br.com.deliveryfacil.model.enums.StatusProduto;
import br.com.deliveryfacil.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProdutoController {
    private final ProdutoService service;

    @PostMapping("/produtos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoDTO.Resposta criar(@Valid @RequestBody ProdutoDTO.Dados dados, @RequestHeader(value = "X-Funcionario-Id", required = false) Long idFuncionario) {
        return ProdutoDTO.Resposta.de(service.criar(dados, idFuncionario));
    }

    @GetMapping("/produtos")
    public List<ProdutoDTO.Resposta> listar(@RequestParam(required = false) String categoria,
                                            @RequestParam(required = false) String status) {
        StatusProduto st = (status == null || status.isBlank()) ? null : EnumRotulo.deRotulo(StatusProduto.class, status);
        return service.listar(categoria, st).stream().map(ProdutoDTO.Resposta::de).toList();
    }

    @GetMapping("/produtos/{id}")
    public ProdutoDTO.Resposta buscar(@PathVariable Long id) {
        return ProdutoDTO.Resposta.de(service.buscar(id));
    }

    @PutMapping("/produtos/{id}")
    public ProdutoDTO.Resposta atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoDTO.Dados dados, @RequestHeader(value = "X-Funcionario-Id", required = false) Long idFuncionario) {
        return ProdutoDTO.Resposta.de(service.atualizar(id, dados, idFuncionario));
    }

    @PatchMapping("/produtos/{id}/status")
    public ProdutoDTO.Resposta alterarStatus(@PathVariable Long id, @Valid @RequestBody ProdutoDTO.AlterarStatus body, @RequestHeader(value = "X-Funcionario-Id", required = false) Long idFuncionario) {
        return ProdutoDTO.Resposta.de(service.alterarStatus(id, body.status(), idFuncionario));
    }

    @PostMapping("/produtos/{id}/grupos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoDTO.GrupoResposta criarGrupo(@PathVariable Long id, @Valid @RequestBody ProdutoDTO.GrupoDados dados, @RequestHeader(value = "X-Funcionario-Id", required = false) Long idFuncionario) {
        return ProdutoDTO.GrupoResposta.de(service.criarGrupo(id, dados, idFuncionario));
    }

    @GetMapping("/produtos/{id}/grupos")
    public List<ProdutoDTO.GrupoResposta> listarGrupos(@PathVariable Long id) {
        return service.listarGrupos(id).stream().map(ProdutoDTO.GrupoResposta::de).toList();
    }

    @DeleteMapping("/grupos/{idGrupo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirGrupo(@PathVariable Long idGrupo, @RequestHeader(value = "X-Funcionario-Id", required = false) Long idFuncionario) {
        service.excluirGrupo(idGrupo, idFuncionario);
    }
}
