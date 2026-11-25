package br.com.deliveryfacil.controller;

import br.com.deliveryfacil.dto.AvaliacaoDTO;
import br.com.deliveryfacil.service.AvaliacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AvaliacaoController {
    private final AvaliacaoService service;

    @PostMapping("/pedidos/{idPedido}/avaliacao")
    @ResponseStatus(HttpStatus.CREATED)
    public AvaliacaoDTO.Resposta criar(@PathVariable Long idPedido, @Valid @RequestBody AvaliacaoDTO.Criar body) {
        return AvaliacaoDTO.Resposta.de(service.criar(idPedido, body));
    }

    @GetMapping("/avaliacoes")
    public List<AvaliacaoDTO.Resposta> listar() {
        return service.listar().stream().map(AvaliacaoDTO.Resposta::de).toList();
    }

    @PatchMapping("/avaliacoes/{id}/resposta")
    public AvaliacaoDTO.Resposta responder(@PathVariable Long id, @Valid @RequestBody AvaliacaoDTO.Responder body, @RequestHeader(value = "X-Funcionario-Id", required = false) Long idFuncionario) {
        return AvaliacaoDTO.Resposta.de(service.responder(id, body.resposta(), idFuncionario));
    }
}
