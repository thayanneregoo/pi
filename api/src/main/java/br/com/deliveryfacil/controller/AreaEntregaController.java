package br.com.deliveryfacil.controller;

import br.com.deliveryfacil.dto.AreaEntregaDTO;
import br.com.deliveryfacil.model.AreaEntrega;
import br.com.deliveryfacil.service.AreaEntregaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/areas-entrega")
@RequiredArgsConstructor
public class AreaEntregaController {
    private final AreaEntregaService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AreaEntrega criar(@Valid @RequestBody AreaEntregaDTO.Dados dados, @RequestHeader(value = "X-Funcionario-Id", required = false) Long idFuncionario) {
        return service.criar(dados, idFuncionario);
    }

    @GetMapping
    public List<AreaEntrega> listar() {
        return service.listar();
    }

    @PutMapping("/{id}")
    public AreaEntrega atualizar(@PathVariable Long id, @Valid @RequestBody AreaEntregaDTO.Dados dados, @RequestHeader(value = "X-Funcionario-Id", required = false) Long idFuncionario) {
        return service.atualizar(id, dados, idFuncionario);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @RequestHeader(value = "X-Funcionario-Id", required = false) Long idFuncionario) {
        service.excluir(id, idFuncionario);
    }
}
