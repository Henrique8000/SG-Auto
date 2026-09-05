package com.sgauto.app.service.estoque;

import com.sgauto.app.dto.estoque.FiltroCategoriaFornecedorDTO;
import com.sgauto.app.model.estoque.CategoriaFornecedor;
import com.sgauto.app.repository.estoque.CategoriaFornecedorRepository;
import com.sgauto.app.specifications.estoque.CategoriaFornecedorSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaFornecedorService {

    private final CategoriaFornecedorRepository categoriaFornecedorRepository;

    public CategoriaFornecedorService(CategoriaFornecedorRepository categoriaFornecedorRepository) {
        this.categoriaFornecedorRepository = categoriaFornecedorRepository;
    }


    @Transactional
    public CategoriaFornecedor cadastrar(CategoriaFornecedor cat) {
        if (cat == null) {
            throw new IllegalArgumentException("Cadastro abortado: os dados da categoria estão nulos.");
        }

        validarDados(cat, null);

        cat.setAtivo(true);

        return categoriaFornecedorRepository.save(cat);
    }

    @Transactional
    public CategoriaFornecedor atualizar(CategoriaFornecedor novo, Long id) {
        if (novo == null) {
            throw new IllegalArgumentException("Atualização abortada: os dados da categoria estão nulos.");
        }

        CategoriaFornecedor categoriaExistente = categoriaFornecedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o ID: " + id));

        validarDados(novo, id);

        categoriaExistente.setNome(novo.getNome().trim());
        categoriaExistente.setDescricao(novo.getDescricao());

        return categoriaFornecedorRepository.save(categoriaExistente);
    }

    @Transactional
    public void alternarStatus(Long id) {
        CategoriaFornecedor categoria = categoriaFornecedorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o ID: " + id));

        categoria.setAtivo(!categoria.getAtivo());
        categoriaFornecedorRepository.save(categoria);
    }


    @Transactional(readOnly = true)
    public Page<CategoriaFornecedor> pesquisar(FiltroCategoriaFornecedorDTO filtro, Pageable pageable) {
        return categoriaFornecedorRepository.findAll(CategoriaFornecedorSpecification.comFiltros(filtro), pageable);
    }


    private void validarDados(CategoriaFornecedor cat, Long idAtual) {
        if (cat.getNome() == null || cat.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }

        String nomeLimpo = cat.getNome().trim();
        cat.setNome(nomeLimpo);

        boolean nomeJaExiste = (idAtual == null)
                ? categoriaFornecedorRepository.existsByNomeIgnoreCase(nomeLimpo)
                : categoriaFornecedorRepository.existsByNomeIgnoreCaseAndIdNot(nomeLimpo, idAtual);

        if (nomeJaExiste) {
            throw new IllegalStateException("Já existe uma categoria cadastrada com o nome: " + nomeLimpo);
        }
    }

    @Transactional(readOnly = true)
    public List<CategoriaFornecedor> listarAtivas() {
        return categoriaFornecedorRepository.findByAtivoTrueOrderByNomeAsc();
    }
}