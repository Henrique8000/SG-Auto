package com.sgauto.app.service;

import com.sgauto.app.model.patio.MotivoEstadia;
import com.sgauto.app.repository.patio.EstadiaPatioRepository;
import com.sgauto.app.repository.patio.MotivoEstadiaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MotivoEstadiaService {

    private final MotivoEstadiaRepository motivoEstadiaRepository;
    private final EstadiaPatioRepository estadiaPatioRepository;

    public MotivoEstadiaService(MotivoEstadiaRepository motivoEstadiaRepository,
                                EstadiaPatioRepository estadiaPatioRepository) {
        this.motivoEstadiaRepository = motivoEstadiaRepository;
        this.estadiaPatioRepository = estadiaPatioRepository;
    }

    @Transactional
    public MotivoEstadia cadastrar(MotivoEstadia motivo) {
        verificarCampos(motivo);
        motivo.setProtegido(false); // protegido só nasce via migration/seed
        motivoEstadiaRepository.findByNome(motivo.getNome()).ifPresent(m -> {
            throw new IllegalArgumentException("Já existe um motivo com este nome.");
        });
        return motivoEstadiaRepository.save(motivo);
    }

    @Transactional
    public MotivoEstadia atualizar(MotivoEstadia motivo) {
        verificarCampos(motivo);
        MotivoEstadia existente = buscarOuFalhar(motivo.getId());
        if (Boolean.TRUE.equals(existente.getProtegido()))
            throw new IllegalStateException("Não é possível editar um motivo protegido pelo sistema.");

        motivoEstadiaRepository.findByNome(motivo.getNome()).ifPresent(m -> {
            if (!m.getId().equals(motivo.getId()))
                throw new IllegalArgumentException("Já existe um motivo com este nome.");
        });
        return motivoEstadiaRepository.save(motivo);
    }

    @Transactional
    public void ativar(Long id) {
        buscarOuFalhar(id).setAtivo(true);
    }

    @Transactional
    public void desativar(Long id) {
        MotivoEstadia motivo = buscarOuFalhar(id);
        garantirNaoEhProtegidoOuEmUso(motivo, "desativar");
        motivo.setAtivo(false);
    }

    @Transactional
    public void excluir(Long id) {
        MotivoEstadia motivo = buscarOuFalhar(id);
        garantirNaoEhProtegidoOuEmUso(motivo, "excluir");
        motivoEstadiaRepository.delete(motivo);
    }

    @Transactional(readOnly = true)
    public List<MotivoEstadia> listarTodos() {
        return motivoEstadiaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MotivoEstadia> listarAtivos() {
        return motivoEstadiaRepository.findByAtivoTrue();
    }

    private void garantirNaoEhProtegidoOuEmUso(MotivoEstadia motivo, String acao) {
        if (Boolean.TRUE.equals(motivo.getProtegido()))
            throw new IllegalStateException("Não é possível " + acao + " um motivo protegido pelo sistema.");
        if (estadiaPatioRepository.existsByMotivoId(motivo.getId()))
            throw new IllegalStateException("Não é possível " + acao + ": existem estadias de pátio associadas a este motivo.");
    }

    private MotivoEstadia buscarOuFalhar(Long id) {
        return motivoEstadiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Motivo não encontrado: " + id));
    }

    private void verificarCampos(MotivoEstadia motivo) {
        if (motivo.getNome() == null || motivo.getNome().trim().isEmpty())
            throw new IllegalArgumentException("O nome do motivo é obrigatório.");
    }
}