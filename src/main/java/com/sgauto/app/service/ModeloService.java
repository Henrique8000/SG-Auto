package com.sgauto.app.service;

import com.sgauto.app.model.Categoria;
import com.sgauto.app.model.Modelo;
import com.sgauto.app.repository.CategoriaRepository;
import com.sgauto.app.repository.ModeloRepository;
import com.sgauto.app.repository.PecaRepository;
import com.sgauto.app.repository.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModeloService {
    private final ModeloRepository modeloRepository;
    private final PecaRepository pecaRepository;

    public ModeloService(ModeloRepository modeloRepository, PecaRepository pecaRepository) {
        this.modeloRepository = modeloRepository;
        this.pecaRepository = pecaRepository;
    }

    @Transactional
    public Modelo cadastrar(Modelo modelo) {
        verificarCampos(modelo);
        String nomeFormatado = formatarNome(modelo.getNome());
        modelo.setNome(nomeFormatado);

        modeloRepository.findByNome(nomeFormatado).ifPresent(c -> {
            throw new IllegalArgumentException("Já existe um modelo com este nome");
        });

        return modeloRepository.save(modelo);
    }

    @Transactional
    public Modelo atualizar(Modelo modelo) {
        verificarCampos(modelo);
        String nomeFormatado = formatarNome(modelo.getNome());
        modelo.setNome(nomeFormatado);

        modeloRepository.findByNome(nomeFormatado).ifPresent(c -> {
            if (!c.getId().equals(modelo.getId())) {
                throw new IllegalArgumentException("Já existe uma categoria com este nome");
            }
        });

        return modeloRepository.save(modelo);
    }

    @Transactional
    public void ativar(Long id) {
        buscarOuFalhar(id).setAtivo(true);
    }

    @Transactional
    public void desativar(Long id) {
        Modelo modelo = buscarOuFalhar(id);
        if (estaEmUso(modelo.getNome())) {
            throw new IllegalStateException("Não é possível desativar: existem peças associadas a este modelo.");
        }
        modelo.setAtivo(false);
    }

    @Transactional
    public void excluir(Long id) {
        Modelo modelo = buscarOuFalhar(id);
        if (estaEmUso(modelo.getNome())) {
            throw new IllegalStateException("Não é possível excluir: existem peças associadas a este modelo.");
        }
        modeloRepository.delete(modelo);
    }

    @Transactional(readOnly = true)
    public boolean estaEmUso(String modelo) {
        return pecaRepository.existsByModelo(modelo);
    }

    @Transactional(readOnly = true)
    public List<Modelo> listarTodas() {
        return modeloRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Modelo> listarAtivas() {
        return modeloRepository.findByAtivoTrue();
    }

    private Modelo buscarOuFalhar(Long id) {
        return modeloRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Modelo não encontrado: " + id));
    }

    private void verificarCampos(Modelo modelo) {
        if (modelo.getNome() == null || modelo.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do modelo é obrigatório");
        }
        if (modelo.getAtivo() == null) {
            throw new IllegalArgumentException("É necessário definir como ATIVO ou INATIVO");
        }
    }

    private String formatarNome(String nome) {
        String nomeTrim = nome.trim();
        return nomeTrim.substring(0, 1).toUpperCase() + nomeTrim.substring(1).toLowerCase();
    }
}
