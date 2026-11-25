package br.com.deliveryfacil.controller;

import br.com.deliveryfacil.dto.PedidoDTO;
import br.com.deliveryfacil.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    private final PedidoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoDTO.Resposta criar(@Valid @RequestBody PedidoDTO.Criar dados) {
        return PedidoDTO.Resposta.de(service.criar(dados));
    }

    @GetMapping
    public List<PedidoDTO.Resposta> listar(@RequestParam(required = false) String status,
                                           @RequestParam(required = false) Long idCliente) {
        return service.listar(status, idCliente).stream().map(PedidoDTO.Resposta::de).toList();
    }

    @GetMapping("/{id}")
    public PedidoDTO.Resposta buscar(@PathVariable Long id) {
        return PedidoDTO.Resposta.de(service.buscar(id));
    }

    @PostMapping("/{id}/itens")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoDTO.Resposta adicionarItem(@PathVariable Long id, @Valid @RequestBody PedidoDTO.Item item) {
        return PedidoDTO.Resposta.de(service.adicionarItem(id, item));
    }

    /** Devolve o pedido já com os totais recalculados. */
    @DeleteMapping("/{id}/itens/{idItem}")
    public PedidoDTO.Resposta removerItem(@PathVariable Long id, @PathVariable Long idItem) {
        return PedidoDTO.Resposta.de(service.removerItem(id, idItem));
    }

    @PatchMapping("/{id}/cupom")
    public PedidoDTO.Resposta aplicarCupom(@PathVariable Long id, @Valid @RequestBody PedidoDTO.Cupom body) {
        return PedidoDTO.Resposta.de(service.aplicarCupom(id, body.codigo()));
    }

    /** Restaurante: avança o fluxo ou cancela (motivo obrigatório). */
    @PatchMapping("/{id}/status")
    public PedidoDTO.Resposta atualizarStatus(@PathVariable Long id, @Valid @RequestBody PedidoDTO.AlterarStatus body, @RequestHeader(value = "X-Funcionario-Id", required = false) Long idFuncionario) {
        return PedidoDTO.Resposta.de(service.atualizarStatus(id, body.status(), body.motivo(), idFuncionario));
    }

    /** Cliente: só cancela enquanto Pendente. */
    @PostMapping("/{id}/cancelamento")
    public PedidoDTO.Resposta cancelar(@PathVariable Long id, @Valid @RequestBody(required = false) PedidoDTO.Cancelamento body) {
        return PedidoDTO.Resposta.de(service.cancelarPeloCliente(id, body == null ? null : body.motivo()));
    }

    @GetMapping("/{id}/historico")
    public List<PedidoDTO.HistoricoResposta> historico(@PathVariable Long id) {
        return service.historico(id).stream().map(PedidoDTO.HistoricoResposta::de).toList();
    }
}
