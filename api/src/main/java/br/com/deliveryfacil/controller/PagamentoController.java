package br.com.deliveryfacil.controller;

import br.com.deliveryfacil.dto.PagamentoDTO;
import br.com.deliveryfacil.service.PagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PagamentoController {
    private final PagamentoService service;

    @PostMapping("/pedidos/{idPedido}/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public PagamentoDTO.Resposta iniciar(@PathVariable Long idPedido, @Valid @RequestBody PagamentoDTO.Criar body) {
        return PagamentoDTO.Resposta.de(service.iniciar(idPedido, body.forma()));
    }

    @GetMapping("/pedidos/{idPedido}/pagamentos")
    public List<PagamentoDTO.Resposta> listar(@PathVariable Long idPedido) {
        return service.listarDoPedido(idPedido).stream().map(PagamentoDTO.Resposta::de).toList();
    }

    /** Webhook do gateway (idempotente). Em produção: validar assinatura do provedor. */
    @PostMapping("/pagamentos/webhook")
    public PagamentoDTO.Resposta webhook(@Valid @RequestBody PagamentoDTO.Webhook body) {
        return PagamentoDTO.Resposta.de(service.processarWebhook(body));
    }
}
