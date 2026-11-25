package br.com.deliveryfacil.service;

import br.com.deliveryfacil.exception.RecursoNaoEncontradoException;
import br.com.deliveryfacil.exception.RegraNegocioException;
import br.com.deliveryfacil.model.Cliente;
import br.com.deliveryfacil.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Verificação de telefone por OTP. O envio de SMS é simulado por log; o código expira em 5 minutos. */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {
    private record Codigo(String valor, Instant expiraEm) {}

    private final ClienteRepository clientes;
    private final Map<Long, Codigo> codigos = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public void gerar(Long idCliente) {
        Cliente c = buscar(idCliente);
        String codigo = String.format("%06d", random.nextInt(1_000_000));
        codigos.put(idCliente, new Codigo(codigo, Instant.now().plusSeconds(300)));
        log.info("[SMS simulado] Código OTP do cliente {} ({}): {}", idCliente, c.getTelefone(), codigo);
    }

    @Transactional
    public Cliente verificar(Long idCliente, String codigoInformado) {
        Cliente c = buscar(idCliente);
        Codigo guardado = codigos.get(idCliente);
        if (guardado == null || Instant.now().isAfter(guardado.expiraEm())) {
            throw new RegraNegocioException("Código expirado ou não solicitado. Solicite um novo código.");
        }
        if (!guardado.valor().equals(codigoInformado)) throw new RegraNegocioException("Código OTP incorreto.");
        codigos.remove(idCliente);
        c.setTelefoneVerificado(true);
        return clientes.save(c);
    }

    private Cliente buscar(Long id) {
        return clientes.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + id));
    }
}
