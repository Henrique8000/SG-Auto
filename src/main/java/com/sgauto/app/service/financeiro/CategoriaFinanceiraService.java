package com.sgauto.app.service.financeiro;

import com.sgauto.app.enums.financeiro.TipoCategoriaFinanceira;
import com.sgauto.app.enums.usuario.PermissaoChave;
import com.sgauto.app.model.financeiro.CategoriaFinanceira;
import com.sgauto.app.repository.financeiro.CategoriaFinanceiraRepository;
import com.sgauto.app.util.VerificaPermissaoUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class CategoriaFinanceiraService {

    private final CategoriaFinanceiraRepository repository;
    private final VerificaPermissaoUtil permissaoUtil;

    public CategoriaFinanceiraService(CategoriaFinanceiraRepository repository, VerificaPermissaoUtil permissaoUtil) {
        this.repository = repository;
        this.permissaoUtil = permissaoUtil;
    }

    @Transactional
    public CategoriaFinanceira salvar(CategoriaFinanceira categoria) {
        validarCategoria(categoria);
        // Garante que uma categoria nova sempre nasça ativa
        if (categoria.getId() == null) {
            categoria.setAtivo(true);
        }
        return repository.save(categoria);
    }

    @Transactional
    public CategoriaFinanceira atualizar(Long id, CategoriaFinanceira dadosAtualizados) {
        if (!permissaoUtil.verificar(PermissaoChave.CATEGORIA_CONTA_GERENCIAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para gerenciar categorias financeiras.");
        }

        CategoriaFinanceira categoriaExistente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));

        if(categoriaExistente.getNome().equals("Receita Automática (Sistema)"))
            throw new IllegalArgumentException("Não é possível alterar a categoria de Receitas Automáticas do sistema");

        validarCategoria(dadosAtualizados);

        categoriaExistente.setNome(dadosAtualizados.getNome());
        categoriaExistente.setTipo(dadosAtualizados.getTipo());

        return repository.save(categoriaExistente);
    }

    public Page<CategoriaFinanceira> listarAtivas(int pagina, int tamanho) {
        return repository.findByAtivoTrue(PageRequest.of(pagina, tamanho));
    }

    public Page<CategoriaFinanceira> listarPorTipos(List<TipoCategoriaFinanceira> tipos, int pagina, int tamanho) {
        return repository.findByTipoInAndAtivoTrue(tipos, PageRequest.of(pagina, tamanho));
    }

    /**
     * Realiza a exclusão lógica (Soft Delete)
     */
    @Transactional
    public void desativar(Long id) {
        if (!permissaoUtil.verificar(PermissaoChave.CATEGORIA_CONTA_GERENCIAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para gerenciar categorias financeiras.");
        }

        CategoriaFinanceira categoria = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));

        CategoriaFinanceira cat = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));

        if(cat.getNome().equals("Receita Automática (Sistema)"))
            throw new IllegalArgumentException("Não é possível desativar a categoria de Receitas Automáticas do sistema");

        categoria.setAtivo(false);
        repository.save(categoria);
    }

    @Transactional
    public void reativar(Long id) {
        if (!permissaoUtil.verificar(PermissaoChave.CATEGORIA_CONTA_GERENCIAR)) {
            throw new IllegalStateException("Seu usuário não possui permissão para gerenciar categorias financeiras.");
        }

        CategoriaFinanceira categoria = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
        categoria.setAtivo(true);
        repository.save(categoria);
    }

    @Transactional(readOnly = true)
    public Page<CategoriaFinanceira> listarComFiltros(String nome, TipoCategoriaFinanceira tipo, boolean ativo, int pagina, int tamanho) {
        String filtroNome = (nome != null && !nome.isBlank())
                ? "%" + nome.trim() + "%"
                : null;

        return repository.buscarComFiltros(filtroNome, tipo, ativo, PageRequest.of(pagina, tamanho));
    }

    @Transactional(readOnly = true)
    public Optional<CategoriaFinanceira> procurarPeloId(Long id){
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<CategoriaFinanceira> procurarPeloNome(String nome){
        return repository.findByNome(nome);
    }

    private void validarCategoria(CategoriaFinanceira categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("Os dados da categoria não podem ser nulos.");
        }
        if (categoria.getNome() == null || categoria.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }
        if (categoria.getTipo() == null) {
            throw new IllegalArgumentException("O tipo da categoria é obrigatório.");
        }
        if(repository.existsByNome(categoria.getNome())){
            throw new IllegalArgumentException("Já existe uma categoria com este nome. Verifique na lista de INATIVAS e ative-a novamente");
        }
    }
}