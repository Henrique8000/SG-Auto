package com.sgauto.app.service;

import com.sgauto.app.model.Categoria;
import com.sgauto.app.repository.CategoriaRepository;
import com.sgauto.app.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        String nomeFormatado = formatarNome(categoria.getNome());

        categoria.setNome(nomeFormatado);

        categoriaRepository.findByNome(nomeFormatado).ifPresent(c -> {
            throw new IllegalArgumentException("Já existe uma categoria com este nome");
        });

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria atualizar(Categoria categoria) {

        verificarCampos(categoria);

        String nomeFormatado = formatarNome(categoria.getNome());

        categoriaRepository.findByNome(nomeFormatado).ifPresent(c -> {
            if (!c.getId().equals(categoria.getId())) {
                throw new IllegalArgumentException("Já existe uma categoria com este nome");
            }
        });

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void ativar(Long id) {
        Categoria categoria = buscarOuFalhar(id);
        categoria.setAtivo(true);
    }

    @Transactional
    public void desativar(Long id) {
        Categoria categoria = buscarOuFalhar(id);
        boolean existeServico = servicoRepository.existsByCategoria(categoria.getNome());
        if (existeServico) {
            throw new IllegalStateException("Não é possível desativar a categoria pois existem serviços associados a ela.");
        }
        categoria.setAtivo(false);
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
            throw new IllegalArgumentException("Selecione o tipo PEÇA, CARRO ou AMBOS");
        }
    }

    private String formatarNome(String nome) {
        String nomeTrim = nome.trim();
        return nomeTrim.substring(0, 1).toUpperCase() + nomeTrim.substring(1).toLowerCase();
    }
}