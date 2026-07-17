package com.sgauto.app.service;

import com.sgauto.app.model.Categoria;
import com.sgauto.app.model.Servico;
import com.sgauto.app.repository.CategoriaRepository;
import com.sgauto.app.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final CategoriaRepository categoriaRepository;

    public ServicoService(ServicoRepository servicoRepository, CategoriaRepository categoriaRepository) {
        this.servicoRepository = servicoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public Servico cadastrar(Servico servico) {
        verificarCampos(servico);

        String nomeFormatado = formatarNome(servico.getNome());
        String categoriaFormatada = formatarNome(servico.getCategoria());

        servico.setNome(nomeFormatado);
        servico.setCategoria(categoriaFormatada);

        servicoRepository.findByCodigo(servico.getCodigo()).ifPresent(c -> {
            throw new IllegalArgumentException("Já existe um Serviço com este código");
        });

        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico atualizar(Servico servico) {

        verificarCampos(servico);

        String nomeFormatado = formatarNome(servico.getNome());
        String categoriaFormatada = formatarNome(servico.getCategoria());

        servico.setNome(nomeFormatado);
        servico.setCategoria(categoriaFormatada);

        servicoRepository.findByCodigo(servico.getCodigo()).ifPresent(s -> {
            if (!s.getId().equals(servico.getId())) {
                throw new IllegalArgumentException("Já existe um Serviço com este código");
            }
        });

        return servicoRepository.save(servico);
    }

    @Transactional
    public void ativar(Long id) {
        Servico servico = buscarOuFalhar(id);
        servico.setAtivo(true);
    }

    @Transactional
    public void desativar(Long id) {
        Servico servico = buscarOuFalhar(id);
        servico.setAtivo(false);
    }

    @Transactional
    public void deletar(Long id){servicoRepository.deleteById(id);}

    @Transactional(readOnly = true)
    public List<Servico> listarTodos() {
        return servicoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Servico> listarAtivos() {
        return servicoRepository.findByAtivo(true);
    }

    @Transactional(readOnly = true)
    public List<Servico> listarInativos() {
        return servicoRepository.findByAtivo(false);
    }

    private Servico buscarOuFalhar(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado: " + id));
    }

    private void verificarCampos(Servico servico){
        if(servico.getCodigo() == null || servico.getCodigo().isEmpty()){
            throw new IllegalArgumentException("O código do serviço é obrigatório");
        }

        if (servico.getNome() == null || servico.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do serviço é obrigatório");
        }


        if (servico.getAtivo() == null){
            throw new IllegalArgumentException("É necessário definir como ATIVO ou INATIVO");
        }

        if (servico.getCategoria() == null || servico.getCategoria().isEmpty()){
            throw new IllegalArgumentException("Selecione uma categoria");
        }

        categoriaRepository.findByNome(servico.getCategoria())
                .filter(Categoria::getAtivo)
                .orElseThrow(() -> new IllegalArgumentException("Categoria inválida ou inativa. Selecione novamente."));

        if(servico.getTempoEstimadoMinutos() == null || servico.getTempoEstimadoMinutos() < 0){
            throw new IllegalArgumentException("Tempo Estimado deve ser preenchido ou igual ou maior que 0 (zero)");
        }

        if(servico.getGarantiaDias() == null || servico.getGarantiaDias() < 0){
            throw new IllegalArgumentException("Garantia deve ser preenchida ou maior que 0 (zero)");
        }

        if (servico.getComissaoPorcentagem() == null
                || servico.getComissaoPorcentagem().compareTo(BigDecimal.ZERO) < 0
                || servico.getComissaoPorcentagem().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("A comissão deve estar entre 0% e 100%.");
        }

        if (servico.getValor() == null || servico.getValor().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O valor do serviço não pode ser negativo");
        }
    }

    private String formatarNome(String nome) {
        String nomeTrim = nome.trim();
        return nomeTrim.substring(0, 1).toUpperCase() + nomeTrim.substring(1).toLowerCase();
    }
}