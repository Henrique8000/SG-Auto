package com.sgauto.app.service;

import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.model.Cliente;
import com.sgauto.app.model.ClientePF;
import com.sgauto.app.model.ClientePJ;
import com.sgauto.app.repository.VeiculoRepository;
import com.sgauto.app.repository.ClienteRepository;
import com.sgauto.app.util.VerificaPermissaoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final VerificaPermissaoUtil permissaoUtil;

    public ClienteService(ClienteRepository clienteRepository, VeiculoRepository veiculoRepository, VerificaPermissaoUtil permissaoUtil) {
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.permissaoUtil = permissaoUtil;
    }

    @Transactional
    public Cliente cadastrar(Cliente cliente) {
        if (!permissaoUtil.verificar(PermissaoChave.CLIENTE_CRIAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para cadastrar clientes.");
        }
        normalizarDocumento(cliente);
        verificarCampos(cliente);

        if (cliente.getDocumento() != null) {
            clienteRepository.findByDocumento(cliente.getDocumento()).ifPresent(c -> {
                throw new IllegalArgumentException("Já existe um cliente com este "
                        + (cliente instanceof ClientePJ ? "CNPJ" : "CPF") + ": " + c.getNome());
            });
        }

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente atualizar(Cliente cliente) {
        if (!permissaoUtil.verificar(PermissaoChave.CLIENTE_EDITAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para atualizar clientes.");
        }
        normalizarDocumento(cliente);
        verificarCampos(cliente);

        if (cliente.getDocumento() != null) {
            clienteRepository.findByDocumento(cliente.getDocumento()).ifPresent(c -> {
                if (!c.getId().equals(cliente.getId())) {
                    throw new IllegalArgumentException("Já existe um cliente com este "
                            + (cliente instanceof ClientePJ ? "CNPJ" : "CPF") + ": " + c.getNome());
                }
            });
        }

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void ativar(Long id) {
        if (!permissaoUtil.verificar(PermissaoChave.CLIENTE_EDITAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para ativar clientes.");
        }
        Cliente cliente = buscarOuFalhar(id);
        cliente.setAtivo(true);
    }

    @Transactional
    public void desativar(Long id) {
        if (!permissaoUtil.verificar(PermissaoChave.CLIENTE_EDITAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para desativar clientes.");
        }
        Cliente cliente = buscarOuFalhar(id);
        cliente.setAtivo(false);
    }

    @Transactional
    public void excluir(Long id) {
        if (!permissaoUtil.verificar(PermissaoChave.CLIENTE_EXCLUIR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para excluir clientes.");
        }
        Cliente cliente = buscarOuFalhar(id);
        if (estaEmUso(id)) {
            throw new IllegalStateException(
                    "Não é possível excluir: este cliente possui veículos ou Ordens de Serviço vinculados. Use Desativar.");
        }
        clienteRepository.delete(cliente);
    }

    @Transactional(readOnly = true)
    public boolean estaEmUso(Long clienteId) {
        // TODO: quando o módulo de Ordem de Serviço existir, adicionar
        //  || ordemServicoRepository.existsByClienteId(clienteId)
        return veiculoRepository.existsByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarAtivos() {
        return clienteRepository.findByAtivo(true);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return buscarOuFalhar(id);
    }

    private Cliente buscarOuFalhar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + id));
    }

    /**
     * Remove máscara do documento e converte vazio em null.
     * CPF: mantém só dígitos. CNPJ: só dígitos e letras maiúsculas
     * (desde jul/2026 a Receita emite CNPJ alfanumérico — os DVs continuam numéricos).
     */
    private void normalizarDocumento(Cliente cliente) {
        String doc = cliente.getDocumento();
        if (doc == null) {
            return;
        }
        doc = cliente instanceof ClientePJ
                ? doc.replaceAll("[^0-9A-Za-z]", "").toUpperCase()
                : doc.replaceAll("\\D", "");
        cliente.setDocumento(doc.isEmpty() ? null : doc);
    }

    private void verificarCampos(Cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException(cliente instanceof ClientePJ
                    ? "A razão social é obrigatória"
                    : "O nome do cliente é obrigatório");
        }
        cliente.setNome(cliente.getNome().trim());

        if (cliente.getAtivo() == null) {
            cliente.setAtivo(true);
        }

        String doc = cliente.getDocumento();
        if (doc != null) {
            if (cliente instanceof ClientePF && !cpfValido(doc)) {
                throw new IllegalArgumentException("CPF inválido. Confira os números digitados.");
            }
            if (cliente instanceof ClientePJ && !cnpjValido(doc)) {
                throw new IllegalArgumentException("CNPJ inválido. Confira os caracteres digitados.");
            }
        }
    }

    /** Valida os dígitos verificadores do CPF (11 dígitos, sem máscara). */
    private boolean cpfValido(String cpf) {
        if (cpf.length() != 11 || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            return false;
        }
        int[] d = cpf.chars().map(c -> c - '0').toArray();

        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += d[i] * (10 - i);
        }
        int dv1 = soma % 11 < 2 ? 0 : 11 - (soma % 11);

        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += d[i] * (11 - i);
        }
        int dv2 = soma % 11 < 2 ? 0 : 11 - (soma % 11);

        return d[9] == dv1 && d[10] == dv2;
    }

    /**
     * Valida os dígitos verificadores do CNPJ (14 posições, sem máscara).
     * Compatível com o CNPJ alfanumérico (letras valem código ASCII - 48;
     * as duas últimas posições são sempre dígitos).
     */
    private boolean cnpjValido(String cnpj) {
        if (cnpj.length() != 14 || !cnpj.matches("[0-9A-Z]{12}\\d{2}")
                || cnpj.chars().distinct().count() == 1) {
            return false;
        }
        int[] v = cnpj.chars().map(c -> c - 48).toArray();

        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += v[i] * pesos1[i];
        }
        int dv1 = soma % 11 < 2 ? 0 : 11 - (soma % 11);

        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += v[i] * pesos2[i];
        }
        int dv2 = soma % 11 < 2 ? 0 : 11 - (soma % 11);

        return v[12] == dv1 && v[13] == dv2;
    }
}