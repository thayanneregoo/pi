package br.com.deliveryfacil.controller;

import br.com.deliveryfacil.dto.FuncionarioDTO;
import br.com.deliveryfacil.service.FuncionarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Cadastro livre apenas para o bootstrap do MVP; em produção exigiria autenticação de gestor. */
@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {
    private final FuncionarioService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuncionarioDTO.Resposta criar(@Valid @RequestBody FuncionarioDTO.Criar dados) {
        return FuncionarioDTO.Resposta.de(service.criar(dados));
    }

    @GetMapping
    public List<FuncionarioDTO.Resposta> listar() {
        return service.listar().stream().map(FuncionarioDTO.Resposta::de).toList();
    }
}
