package com.sgauto.app.model.OrdemServico;

import com.sgauto.app.enums.StatusOS;
import com.sgauto.app.model.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_ordem_servico")
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamentos Principais
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusOS status = StatusOS.ABERTA;

    @Column(name = "sintomas_relatados", columnDefinition = "TEXT")
    private String sintomasRelatados;

    @Column(name = "observacoes_internas", columnDefinition = "TEXT")
    private String observacoesInternas;

    @Column(name = "motivo_pausa")
    private String motivoPausa;

    @Column(name = "ficar_no_patio", nullable = false)
    private boolean ficarNoPatio = false;

    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDateTime dataAbertura = LocalDateTime.now();

    @Column(name = "data_previsao")
    private LocalDateTime dataPrevisao;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    // Financeiro
    @Column(name = "valor_total_pecas", nullable = false)
    private BigDecimal valorTotalPecas = BigDecimal.ZERO;

    @Column(name = "valor_total_servicos", nullable = false)
    private BigDecimal valorTotalServicos = BigDecimal.ZERO;

    @Column(name = "valor_desconto", nullable = false)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_total_os", nullable = false)
    private BigDecimal valorTotalOs = BigDecimal.ZERO;

    // Relacionamentos Bidirecionais
    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OsPeca> pecas = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OsServico> servicos = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OsPagamento> pagamentos = new ArrayList<>();

    // Construtor Vazio (Obrigatório para o JPA)
    public OrdemServico() {
    }

    // Construtor com Argumentos (Mantido com a sua assinatura original)
    public OrdemServico(BigDecimal valorDesconto, BigDecimal valorTotalOs, BigDecimal valorTotalPecas, BigDecimal valorTotalServicos, Veiculo veiculo, StatusOS status, String sintomasRelatados, List<OsServico> servicos, List<OsPagamento> pagamentos, List<OsPeca> pecas, String observacoesInternas, String motivoPausa, Long id, Funcionario funcionario, boolean ficarNoPatio, LocalDateTime dataPrevisao, LocalDateTime dataConclusao, LocalDateTime dataAbertura, Cliente cliente) {
        this.valorDesconto = valorDesconto;
        this.valorTotalOs = valorTotalOs;
        this.valorTotalPecas = valorTotalPecas;
        this.valorTotalServicos = valorTotalServicos;
        this.veiculo = veiculo;
        this.status = status;
        this.sintomasRelatados = sintomasRelatados;
        this.servicos = servicos;
        this.pagamentos = pagamentos;
        this.pecas = pecas;
        this.observacoesInternas = observacoesInternas;
        this.motivoPausa = motivoPausa;
        this.id = id;
        this.funcionario = funcionario;
        this.ficarNoPatio = ficarNoPatio;
        this.dataPrevisao = dataPrevisao;
        this.dataConclusao = dataConclusao;
        this.dataAbertura = dataAbertura;
        this.cliente = cliente;
    }

    // ==========================================
    // GETTERS E SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public StatusOS getStatus() {
        return status;
    }

    public void setStatus(StatusOS status) {
        this.status = status;
    }

    public String getSintomasRelatados() {
        return sintomasRelatados;
    }

    public void setSintomasRelatados(String sintomasRelatados) {
        this.sintomasRelatados = sintomasRelatados;
    }

    public String getObservacoesInternas() {
        return observacoesInternas;
    }

    public void setObservacoesInternas(String observacoesInternas) {
        this.observacoesInternas = observacoesInternas;
    }

    public String getMotivoPausa() {
        return motivoPausa;
    }

    public void setMotivoPausa(String motivoPausa) {
        this.motivoPausa = motivoPausa;
    }

    public boolean isFicarNoPatio() {
        return ficarNoPatio;
    }

    public void setFicarNoPatio(boolean ficarNoPatio) {
        this.ficarNoPatio = ficarNoPatio;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataPrevisao() {
        return dataPrevisao;
    }

    public void setDataPrevisao(LocalDateTime dataPrevisao) {
        this.dataPrevisao = dataPrevisao;
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDateTime dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    public BigDecimal getValorTotalPecas() {
        return valorTotalPecas;
    }

    public void setValorTotalPecas(BigDecimal valorTotalPecas) {
        this.valorTotalPecas = valorTotalPecas;
    }

    public BigDecimal getValorTotalServicos() {
        return valorTotalServicos;
    }

    public void setValorTotalServicos(BigDecimal valorTotalServicos) {
        this.valorTotalServicos = valorTotalServicos;
    }

    public BigDecimal getValorDesconto() {
        return valorDesconto;
    }

    public void setValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public BigDecimal getValorTotalOs() {
        return valorTotalOs;
    }

    public void setValorTotalOs(BigDecimal valorTotalOs) {
        this.valorTotalOs = valorTotalOs;
    }

    public List<OsPeca> getPecas() {
        return pecas;
    }

    public void setPecas(List<OsPeca> pecas) {
        this.pecas = pecas;
    }

    public List<OsServico> getServicos() {
        return servicos;
    }

    public void setServicos(List<OsServico> servicos) {
        this.servicos = servicos;
    }

    public List<OsPagamento> getPagamentos() {
        return pagamentos;
    }

    public void setPagamentos(List<OsPagamento> pagamentos) {
        this.pagamentos = pagamentos;
    }
}