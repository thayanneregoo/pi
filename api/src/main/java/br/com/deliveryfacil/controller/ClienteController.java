package br.com.deliveryfacil.controller;

import br.com.deliveryfacil.dto.ClienteDTO;
import br.com.deliveryfacil.service.ClienteService;
import br.com.deliveryfacil.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService service;
    private final OtpService otp;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteDTO.Resposta criar(@Valid @RequestBody ClienteDTO.Criar dados) {
        return ClienteDTO.Resposta.de(service.criar(dados));
    }

    @GetMapping
    public List<ClienteDTO.Resposta> listar() {
        return service.listar().stream().map(ClienteDTO.Resposta::de).toList();
    }

    @GetMapping("/{id}")
    public ClienteDTO.Resposta buscar(@PathVariable Long id) {
        return ClienteDTO.Resposta.de(service.buscar(id));
    }

    @PutMapping("/{id}")
    public ClienteDTO.Resposta atualizar(@PathVariable Long id, @Valid @RequestBody ClienteDTO.Atualizar dados) {
        return ClienteDTO.Resposta.de(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    /** Gera e "envia" o código OTP (simulado em log). */
    @PostMapping("/{id}/otp")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void solicitarOtp(@PathVariable Long id) {
        otp.gerar(id);
    }

    @PatchMapping("/{id}/telefone-verificado")
    public ClienteDTO.Resposta verificarOtp(@PathVariable Long id, @Valid @RequestBody ClienteDTO.VerificarOtp body) {
        return ClienteDTO.Resposta.de(otp.verificar(id, body.codigo()));
    }
}
