package com.sgauto.app.service;

import com.sgauto.app.enums.TipoAjustePreco;
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
    public void excluir(Long id) {
        Servico servico = buscarOuFalhar(id);
        if (estaEmUso(id)) {
            throw new IllegalStateException("Não é possível excluir: este serviço está vinculado a uma ou mais Ordens de Serviço.");
        }
        servicoRepository.delete(servico);
    }

    @Transactional(readOnly = true)
    public boolean estaEmUso(Long servicoId) {
        // TODO: quando o módulo de Ordem de Serviço existir, trocar por uma
        return false;
    }

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

    @Transactional
    public int ajustarValoresEmMassa(List<Long> idsServicos, TipoAjustePreco tipo, BigDecimal valor) {
        if (idsServicos == null || idsServicos.isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos um serviço para ajustar.");
        }
        if (valor == null) {
            throw new IllegalArgumentException("Informe o valor do ajuste.");
        }

        List<Servico> servicos = servicoRepository.findAllById(idsServicos);
        for (Servico servico : servicos) {
            BigDecimal novoValor = (tipo == TipoAjustePreco.VALOR_FIXO)
                    ? servico.getValor().add(valor)
                    : servico.getValor().multiply(BigDecimal.ONE.add(valor.divide(new BigDecimal("100"))))
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            if (novoValor.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("O ajuste resultaria em valor negativo. Revise o valor informado.");
            }
            servico.setValor(novoValor);
        }
        servicoRepository.saveAll(servicos);
        return servicos.size();
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