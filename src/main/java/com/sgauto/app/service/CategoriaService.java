package com.sgauto.app.service;

import com.sgauto.app.model.Categoria;
import com.sgauto.app.repository.CategoriaRepository;
import com.sgauto.app.repository.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ServicoRepository servicoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ServicoRepository servicoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.servicoRepository = servicoRepository;
    }

    @Transactional
    public Categoria cadastrar(Categoria categoria) {

        verificarCampos(categoria);

        String nomeTrim = categoria.getNome().trim();
        String nomeFormatado = nomeTrim.substring(0, 1).toUpperCase() + nomeTrim.substring(1).toLowerCase();

        categoria.setNome(nomeFormatado);

        categoriaRepository.findByNome(nomeFormatado).ifPresent(c -> {
            throw new IllegalArgumentException("Já existe uma categoria com este nome");
        });

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria atualizar(Categoria categoria) {

        verificarCampos(categoria);

        String nomeTrim = categoria.getNome().trim();
        String nomeFormatado = nomeTrim.substring(0, 1).toUpperCase() + nomeTrim.substring(1).toLowerCase();

        categoriaRepository.findByNome(nomeFormatado).ifPresent(c -> {
            throw new IllegalArgumentException("Já existe uma categoria com este nome");
        });

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void desativar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o ID: " + id));

        boolean existeServico = servicoRepository.existsByCategoria(categoria.getNome());
        if (existeServico) {
            throw new IllegalStateException("Não é possível desativar a categoria pois existem serviços associados a ela.");
        }

        // 2. Desativação (Soft Delete): Altera o status da categoria
        categoria.setAtivo(false);

        // Opcional: categoriaRepository.save(categoria);
        // (Como usamos @Transactional, o JPA faz o "Dirty Checking" e atualiza no banco automaticamente)
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarAtivas() {
        return categoriaRepository.findByAtivoTrue();
    }

    private Categoria buscarOuFalhar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: " + id));
    }

    private void verificarCampos(Categoria categoria){
        if (categoria.getNome() == null || categoria.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório");
        }

        if (categoria.getAtivo() == null){
            throw new IllegalArgumentException("É necessário definir como ATIVO ou INATIVO");
        }

        if (categoria.getTipo() == null || categoria.getTipo().isEmpty()){
            throw new IllegalArgumentException("Selecione o tipo \"PEÇA\" ou \"CARRO\". Caso não selecione nenhum dos dois será definido como \"AMBOS\"");
        }
    }
}