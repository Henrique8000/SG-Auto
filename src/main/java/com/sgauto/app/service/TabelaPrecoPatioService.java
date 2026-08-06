package com.sgauto.app.service;

import com.sgauto.app.model.patio.TabelaPrecoPatio;
import com.sgauto.app.repository.patio.EstadiaPatioRepository;
import com.sgauto.app.repository.patio.TabelaPrecoPatioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TabelaPrecoPatioService {

    private static final String DESCRICAO_TARIFA_PADRAO_OS = "Tarifa Padrão - Ordem de Serviço (sem cobrança)";

    private final TabelaPrecoPatioRepository tabelaPrecoPatioRepository;
    private final EstadiaPatioRepository estadiaPatioRepository;

    public TabelaPrecoPatioService(TabelaPrecoPatioRepository tabelaPrecoPatioRepository,
                                   EstadiaPatioRepository estadiaPatioRepository) {
        this.tabelaPrecoPatioRepository = tabelaPrecoPatioRepository;
        this.estadiaPatioRepository = estadiaPatioRepository;
    }

    @Transactional
    public TabelaPrecoPatio cadastrar(TabelaPrecoPatio tarifa) {
        verificarCampos(tarifa);
        if (DESCRICAO_TARIFA_PADRAO_OS.equalsIgnoreCase(tarifa.getDescricao().trim())) {
            throw new IllegalArgumentException("Esta descrição é reservada para a tarifa padrão gerada pelo sistema.");
        }
        return tabelaPrecoPatioRepository.save(tarifa);
    }

    @Transactional
    public TabelaPrecoPatio atualizar(TabelaPrecoPatio tarifa) {
        verificarCampos(tarifa);
        TabelaPrecoPatio existente = buscarOuFalhar(tarifa.getId());
        if (DESCRICAO_TARIFA_PADRAO_OS.equals(existente.getDescricao())) {
            throw new IllegalStateException("Não é possível editar a tarifa padrão usada pelas entradas automáticas de O.S.");
        }
        return tabelaPrecoPatioRepository.save(tarifa);
    }

    @Transactional
    public void ativar(Long id) {
        buscarOuFalhar(id).setAtivo(true);
    }

    @Transactional
    public void desativar(Long id) {
        TabelaPrecoPatio tarifa = buscarOuFalhar(id);
        garantirNaoEhProtegidaOuEmUso(tarifa, "desativar");
        tarifa.setAtivo(false);
    }

    @Transactional
    public void excluir(Long id) {
        TabelaPrecoPatio tarifa = buscarOuFalhar(id);
        garantirNaoEhProtegidaOuEmUso(tarifa, "excluir");
        tabelaPrecoPatioRepository.delete(tarifa);
    }

    @Transactional(readOnly = true)
    public List<TabelaPrecoPatio> listarTodas() {
        return tabelaPrecoPatioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TabelaPrecoPatio> listarAtivas() {
        return tabelaPrecoPatioRepository.findByAtivoTrue();
    }

    private void garantirNaoEhProtegidaOuEmUso(TabelaPrecoPatio tarifa, String acao) {
        if (DESCRICAO_TARIFA_PADRAO_OS.equals(tarifa.getDescricao())) {
            throw new IllegalStateException("Não é possível " + acao + " a tarifa padrão usada pelas entradas automáticas de O.S.");
        }
        if (estadiaPatioRepository.existsByTarifaId(tarifa.getId())) {
            throw new IllegalStateException("Não é possível " + acao + ": existem estadias de pátio associadas a esta tarifa.");
        }
    }

    private TabelaPrecoPatio buscarOuFalhar(Long id) {
        return tabelaPrecoPatioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarifa não encontrada: " + id));
    }

    private void verificarCampos(TabelaPrecoPatio tarifa) {
        if (tarifa.getDescricao() == null || tarifa.getDescricao().trim().isEmpty())
            throw new IllegalArgumentException("A descrição da tarifa é obrigatória.");
        if (tarifa.getCategoria() == null)
            throw new IllegalArgumentException("A categoria é obrigatória.");
        if (tarifa.getValorDiaria() == null || tarifa.getValorDiaria().signum() < 0)
            throw new IllegalArgumentException("O valor da diária não pode ser negativo.");
        if (tarifa.getDiasCarencia() == null || tarifa.getDiasCarencia() < 0)
            throw new IllegalArgumentException("Os dias de carência não podem ser negativos.");
    }
}