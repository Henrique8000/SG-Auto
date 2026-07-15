package com.sgauto.app.service;

import com.sgauto.app.model.Peca;
import com.sgauto.app.repository.PecaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstoqueService {

    private final PecaRepository pecaRepository;

    public EstoqueService(PecaRepository pecaRepository) {
        this.pecaRepository = pecaRepository;
    }

    @Transactional
    public Peca cadastrarPeca(Peca peca) {
        pecaRepository.findByCodigo(peca.getCodigo()).ifPresent(p -> {
            throw new IllegalArgumentException("Já existe uma peça com esse código.");
        });
        peca.setModelo(normalizarModelo(peca.getModelo()));
        return pecaRepository.save(peca);
    }

    @Transactional
    public void darEntradaEstoque(Long id, int quantidade) {
        Peca peca = buscarOuFalhar(id);
        peca.setQuantidadeEstoque(peca.getQuantidadeEstoque() + quantidade);
        pecaRepository.save(peca);
    }

    @Transactional
    public void darSaidaEstoque(Long id, int quantidade) {
        Peca peca = buscarOuFalhar(id);
        int nova = peca.getQuantidadeEstoque() - quantidade;
        if (nova < 0) {
            throw new IllegalStateException("Estoque insuficiente para: " + peca.getDescricao());
        }
        peca.setQuantidadeEstoque(nova);
        pecaRepository.save(peca);
    }

    @Transactional
    public Peca atualizar(Peca peca) {
        peca.setModelo(normalizarModelo(peca.getModelo()));
        return pecaRepository.save(peca);
    }

    @Transactional
    public void excluir(Long id) {
        pecaRepository.deleteById(id);
    }

    private String normalizarModelo(String modelo) {
        return (modelo == null || modelo.isBlank()) ? "Geral" : modelo.trim();
    }

    @Transactional(readOnly = true)
    public List<Peca> listarTodas() {
        return pecaRepository.findAll();
    }

    public List<String> listarModelosDistintos() {
        return listarTodas().stream()
                .map(Peca::getModelo)
                .distinct()
                .sorted()
                .toList();
    }

    private Peca buscarOuFalhar(Long id) {
        return pecaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Peça não encontrada: " + id));
    }


}