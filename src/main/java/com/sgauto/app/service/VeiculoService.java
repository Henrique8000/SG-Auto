package com.sgauto.app.service;

import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.model.Cliente;
import com.sgauto.app.model.Veiculo;
import com.sgauto.app.repository.ClienteRepository;
import com.sgauto.app.repository.ModeloRepository;
import com.sgauto.app.repository.VeiculoRepository;
import com.sgauto.app.util.VerificaPermissaoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
    private final ModeloRepository modeloRepository;
    private final VerificaPermissaoUtil permissaoUtil;

    public VeiculoService(VeiculoRepository veiculoRepository,
                          ClienteRepository clienteRepository,
                          ModeloRepository modeloRepository, VerificaPermissaoUtil permissaoUtil) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
        this.modeloRepository = modeloRepository;
        this.permissaoUtil = permissaoUtil;
    }

    @Transactional
    public Veiculo cadastrar(Veiculo veiculo) {
        if (!permissaoUtil.verificar(PermissaoChave.VEICULO_CRIAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para cadastrar veículos.");
        }
        normalizar(veiculo);
        verificarCampos(veiculo);

        veiculoRepository.findByPlaca(veiculo.getPlaca()).ifPresent(v -> {
            throw new IllegalArgumentException("Já existe um veículo com a placa " + veiculo.getPlaca());
        });

        return veiculoRepository.save(veiculo);
    }

    @Transactional
    public Veiculo atualizar(Veiculo veiculo) {
        if (!permissaoUtil.verificar(PermissaoChave.VEICULO_EDITAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para atualizar veículos.");
        }
        normalizar(veiculo);
        verificarCampos(veiculo);

        veiculoRepository.findByPlaca(veiculo.getPlaca()).ifPresent(v -> {
            if (!v.getId().equals(veiculo.getId())) {
                throw new IllegalArgumentException("Já existe um veículo com a placa " + veiculo.getPlaca());
            }
        });

        return veiculoRepository.save(veiculo);
    }

    @Transactional
    public void ativar(Long id) {
        if (!permissaoUtil.verificar(PermissaoChave.VEICULO_EDITAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para ativar veículos.");
        }
        buscarOuFalhar(id).setAtivo(true);
    }

    @Transactional
    public void desativar(Long id) {
        if (!permissaoUtil.verificar(PermissaoChave.VEICULO_EDITAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para desativar veículos.");
        }
        buscarOuFalhar(id).setAtivo(false);
    }

    @Transactional
    public void excluir(Long id) {
        if (!permissaoUtil.verificar(PermissaoChave.VEICULO_EXCLUIR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para excluir veículos.");
        }
        Veiculo veiculo = buscarOuFalhar(id);
        if (estaEmUso(id)) {
            throw new IllegalStateException(
                    "Não é possível excluir: este veículo possui Ordens de Serviço ou registros de pátio vinculados. Use Desativar.");
        }
        veiculoRepository.delete(veiculo);
    }

    @Transactional(readOnly = true)
    public boolean estaEmUso(Long veiculoId) {
        // TODO: quando os módulos de Ordem de Serviço e Pátio referenciarem t_veiculo por FK,
        //  trocar por ordemServicoRepository.existsByVeiculoId(veiculoId) || estadiaRepository.existsByVeiculoId(veiculoId)
        return false;
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarTodos() {
        return veiculoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarAtivos() {
        return veiculoRepository.findByAtivo(true);
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarPorCliente(Long clienteId) {
        return veiculoRepository.findByClienteId(clienteId);
    }

    private Veiculo buscarOuFalhar(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado: " + id));
    }

    private void normalizar(Veiculo veiculo) {
        if (veiculo.getPlaca() != null) {
            veiculo.setPlaca(veiculo.getPlaca().replaceAll("[^A-Za-z0-9]", "").toUpperCase());
        }
        if (veiculo.getMarca() != null) {
            veiculo.setMarca(veiculo.getMarca().trim());
        }
        if (veiculo.getModelo() != null) {
            veiculo.setModelo(veiculo.getModelo().trim());
        }
        if (veiculo.getAtivo() == null) {
            veiculo.setAtivo(true);
        }
    }

    private void verificarCampos(Veiculo veiculo) {
        // Dono (cliente) — precisa existir de fato no banco
        if (veiculo.getCliente() == null || veiculo.getCliente().getId() == null) {
            throw new IllegalArgumentException("Selecione o dono do veículo.");
        }
        Cliente dono = clienteRepository.findById(veiculo.getCliente().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente informado não existe."));
        veiculo.setCliente(dono);

        // Placa — obrigatória e no formato antigo (AAA9999) ou Mercosul (AAA9A99)
        if (veiculo.getPlaca() == null || veiculo.getPlaca().isEmpty()) {
            throw new IllegalArgumentException("A placa é obrigatória.");
        }
        if (!placaValida(veiculo.getPlaca())) {
            throw new IllegalArgumentException("Placa inválida. Use o formato ABC1234 ou ABC1D23.");
        }

        // Marca
        if (veiculo.getMarca() == null || veiculo.getMarca().isEmpty()) {
            throw new IllegalArgumentException("A marca é obrigatória.");
        }

        // Modelo — precisa existir e estar ativo no catálogo t_modelo
        if (veiculo.getModelo() == null || veiculo.getModelo().isEmpty()) {
            throw new IllegalArgumentException("Selecione o modelo.");
        }
        modeloRepository.findByNome(veiculo.getModelo())
                .filter(m -> Boolean.TRUE.equals(m.getAtivo()))
                .orElseThrow(() -> new IllegalArgumentException("Modelo inválido ou inativo. Selecione novamente."));

        // Ano — opcional, mas se vier tem que ser plausível
        if (veiculo.getAno() != null) {
            int anoAtual = Year.now().getValue();
            if (veiculo.getAno() < 1900 || veiculo.getAno() > anoAtual + 1) {
                throw new IllegalArgumentException("Ano inválido. Informe um valor entre 1900 e " + (anoAtual + 1) + ".");
            }
        }

        // KM — opcional, mas não pode ser negativo
        if (veiculo.getKm() != null && veiculo.getKm() < 0) {
            throw new IllegalArgumentException("A quilometragem não pode ser negativa.");
        }
    }

    /** Aceita placa antiga (AAA9999) e Mercosul (AAA9A99), sem máscara e em maiúsculas. */
    private boolean placaValida(String placa) {
        return placa.matches("[A-Z]{3}[0-9]{4}") || placa.matches("[A-Z]{3}[0-9][A-Z][0-9]{2}");
    }
}