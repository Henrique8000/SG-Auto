package com.sgauto.app.model;

import com.sgauto.app.enums.CargoFuncionario;
import com.sgauto.app.enums.StatusFuncionario;
import com.sgauto.app.enums.TipoContratoFuncionario;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "t_funcionario")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "funcionario_id")
    private Long id;

    @NotBlank(message = "A matrícula é obrigatória.")
    @Size(max = 20)
    @Column(name = "funcionario_matricula", nullable = false, unique = true, length = 20)
    private String matricula;

    @NotBlank(message = "O nome completo é obrigatório.")
    @Size(max = 150)
    @Column(name = "funcionario_nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Size(max = 150)
    @Column(name = "funcionario_nome_social", length = 150)
    private String nomeSocial;

    @NotBlank(message = "O CPF é obrigatório.")
    @Pattern(regexp = "^[0-9]{11}$", message = "O CPF deve conter exatamente 11 dígitos numéricos.")
    @Column(name = "funcionario_cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @Size(max = 20)
    @Column(name = "funcionario_rg", length = 20)
    private String rg;

    @Column(name = "funcionario_data_nascimento")
    private LocalDate dataNascimento;

    @Size(max = 20)
    @Column(name = "funcionario_genero", length = 20)
    private String genero;

    @Size(max = 15)
    @Column(name = "funcionario_telefone_fixo", length = 15)
    private String telefoneFixo;

    @NotBlank(message = "O celular é obrigatório.")
    @Size(max = 15)
    @Column(name = "funcionario_celular", nullable = false, length = 15)
    private String celular;

    @Email(message = "E-mail em formato inválido.")
    @Size(max = 150)
    @Column(name = "funcionario_email", length = 150)
    private String email;

    @Size(max = 9)
    @Column(name = "funcionario_cep", length = 9)
    private String cep;

    @Size(max = 150)
    @Column(name = "funcionario_logradouro", length = 150)
    private String logradouro;

    @Size(max = 10)
    @Column(name = "funcionario_numero", length = 10)
    private String numero;

    @Size(max = 100)
    @Column(name = "funcionario_complemento", length = 100)
    private String complemento;

    @Size(max = 100)
    @Column(name = "funcionario_bairro", length = 100)
    private String bairro;

    @Size(max = 100)
    @Column(name = "funcionario_cidade", length = 100)
    private String cidade;

    @Pattern(regexp = "^[A-Z]{2}$", message = "O estado deve ter 2 letras maiúsculas (UF).")
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "funcionario_estado", length = 2, columnDefinition = "char(2)")
    private String estado;

    @NotNull(message = "O cargo é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(name = "funcionario_cargo", nullable = false, length = 30)
    private CargoFuncionario cargo;

    @Size(max = 150)
    @Column(name = "funcionario_especialidade", length = 150)
    private String especialidade;

    @NotNull(message = "O tipo de contrato é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(name = "funcionario_tipo_contrato", nullable = false, length = 20)
    private TipoContratoFuncionario tipoContrato = TipoContratoFuncionario.CLT;

    @NotNull(message = "A data de admissão é obrigatória.")
    @Column(name = "funcionario_data_admissao", nullable = false)
    private LocalDate dataAdmissao = LocalDate.now();

    @Column(name = "funcionario_data_demissao")
    private LocalDate dataDemissao;

    @Min(0) @Max(168)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "funcionario_carga_horaria_semanal")
    private Integer cargaHorariaSemanal = 44;

    @NotNull
    @Column(name = "funcionario_exibe_em_os", nullable = false)
    private Boolean exibeEmOs = true;

    @PositiveOrZero(message = "O custo hora não pode ser negativo.")
    @Column(name = "funcionario_custo_hora", precision = 10, scale = 2)
    private BigDecimal custoHora;

    @PositiveOrZero(message = "O salário não pode ser negativo.")
    @Column(name = "funcionario_salario_base", precision = 10, scale = 2)
    private BigDecimal salarioBase;

    @NotNull
    @DecimalMin("0.00") @DecimalMax("100.00")
    @Column(name = "funcionario_comissao_percentual", nullable = false, precision = 5, scale = 2)
    private BigDecimal comissaoPercentual = BigDecimal.ZERO;

    @Size(max = 20)
    @Column(name = "funcionario_numero_cnh", length = 20)
    private String numeroCnh;

    @Size(max = 5)
    @Column(name = "funcionario_categoria_cnh", length = 5)
    private String categoriaCnh;

    @Column(name = "funcionario_validade_cnh")
    private LocalDate validadeCnh;

    @NotNull(message = "O status é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(name = "funcionario_status", nullable = false, length = 20)
    private StatusFuncionario status = StatusFuncionario.ATIVO;

    @NotNull
    @Column(name = "funcionario_ativo", nullable = false)
    private Boolean ativo = true;

    @Size(max = 255)
    @Column(name = "funcionario_foto_url", length = 255)
    private String fotoUrl;

    @Column(name = "funcionario_observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "funcionario_data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @Column(name = "funcionario_data_atualizacao", nullable = false)
    private OffsetDateTime dataAtualizacao;

    @Column(name = "funcionario_removido_em")
    private OffsetDateTime removidoEm;

    public Funcionario() {}

    @PrePersist
    protected void aoCriar() {
        OffsetDateTime agora = OffsetDateTime.now();
        this.dataCriacao = agora;
        this.dataAtualizacao = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        this.dataAtualizacao = OffsetDateTime.now();
    }
    
    public String getNomeExibicao() {
        return (nomeSocial != null && !nomeSocial.isBlank()) ? nomeSocial : nomeCompleto;
    }

    public boolean isAptoParaOrdemServico() {
        return this.removidoEm == null
                && Boolean.TRUE.equals(this.ativo)
                && Boolean.TRUE.equals(this.exibeEmOs)
                && this.status == StatusFuncionario.ATIVO;
    }

    // ---------------------------------------------------------------------
    // Getters e Setters
    // ---------------------------------------------------------------------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getNomeSocial() { return nomeSocial; }
    public void setNomeSocial(String nomeSocial) { this.nomeSocial = nomeSocial; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getTelefoneFixo() { return telefoneFixo; }
    public void setTelefoneFixo(String telefoneFixo) { this.telefoneFixo = telefoneFixo; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public CargoFuncionario getCargo() { return cargo; }
    public void setCargo(CargoFuncionario cargo) { this.cargo = cargo; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public TipoContratoFuncionario getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(TipoContratoFuncionario tipoContrato) { this.tipoContrato = tipoContrato; }

    public LocalDate getDataAdmissao() { return dataAdmissao; }
    public void setDataAdmissao(LocalDate dataAdmissao) { this.dataAdmissao = dataAdmissao; }

    public LocalDate getDataDemissao() { return dataDemissao; }
    public void setDataDemissao(LocalDate dataDemissao) { this.dataDemissao = dataDemissao; }

    public Integer getCargaHorariaSemanal() { return cargaHorariaSemanal; }
    public void setCargaHorariaSemanal(Integer cargaHorariaSemanal) { this.cargaHorariaSemanal = cargaHorariaSemanal; }

    public Boolean getExibeEmOs() { return exibeEmOs; }
    public void setExibeEmOs(Boolean exibeEmOs) { this.exibeEmOs = exibeEmOs; }

    public BigDecimal getCustoHora() { return custoHora; }
    public void setCustoHora(BigDecimal custoHora) { this.custoHora = custoHora; }

    public BigDecimal getSalarioBase() { return salarioBase; }
    public void setSalarioBase(BigDecimal salarioBase) { this.salarioBase = salarioBase; }

    public BigDecimal getComissaoPercentual() { return comissaoPercentual; }
    public void setComissaoPercentual(BigDecimal comissaoPercentual) { this.comissaoPercentual = comissaoPercentual; }

    public String getNumeroCnh() { return numeroCnh; }
    public void setNumeroCnh(String numeroCnh) { this.numeroCnh = numeroCnh; }

    public String getCategoriaCnh() { return categoriaCnh; }
    public void setCategoriaCnh(String categoriaCnh) { this.categoriaCnh = categoriaCnh; }

    public LocalDate getValidadeCnh() { return validadeCnh; }
    public void setValidadeCnh(LocalDate validadeCnh) { this.validadeCnh = validadeCnh; }

    public StatusFuncionario getStatus() { return status; }
    public void setStatus(StatusFuncionario status) { this.status = status; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public OffsetDateTime getRemovidoEm() {
        return removidoEm;
    }

    public void setRemovidoEm(OffsetDateTime removidoEm) {
        this.removidoEm = removidoEm;
    }

    public OffsetDateTime getDataCriacao() { return dataCriacao; }
    public OffsetDateTime getDataAtualizacao() { return dataAtualizacao; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Funcionario that = (Funcionario) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}