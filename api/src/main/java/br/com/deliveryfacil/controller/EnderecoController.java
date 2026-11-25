package br.com.deliveryfacil.controller;

import br.com.deliveryfacil.dto.EnderecoDTO;
import br.com.deliveryfacil.service.EnderecoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnderecoController {
    private final EnderecoService service;

    @PostMapping("/clientes/{idCliente}/enderecos")
    @ResponseStatus(HttpStatus.CREATED)
    public EnderecoDTO.Resposta criar(@PathVariable Long idCliente, @Valid @RequestBody EnderecoDTO.Dados dados) {
        return EnderecoDTO.Resposta.de(service.criar(idCliente, dados));
    }

    @GetMapping("/clientes/{idCliente}/enderecos")
    public List<EnderecoDTO.Resposta> listar(@PathVariable Long idCliente) {
        return service.listarDoCliente(idCliente).stream().map(EnderecoDTO.Resposta::de).toList();
    }

    @PutMapping("/enderecos/{id}")
    public EnderecoDTO.Resposta atualizar(@PathVariable Long id, @Valid @RequestBody EnderecoDTO.Dados dados) {
        return EnderecoDTO.Resposta.de(service.atualizar(id, dados));
    }

    @DeleteMapping("/enderecos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
