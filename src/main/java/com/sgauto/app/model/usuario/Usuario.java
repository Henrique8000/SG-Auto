package com.sgauto.app.model.usuario;

import com.sgauto.app.model.Funcionario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_login", nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "usuario_senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Column(name = "usuario_nome_exibicao", nullable = false, length = 150)
    private String nomeExibicao;

    @Column(name = "usuario_email", length = 150)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_funcionario_id")
    private Funcionario funcionario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_perfil_id", nullable = false)
    private PerfilAcesso perfil;

    @Column(name = "usuario_ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "usuario_deve_trocar_senha", nullable = false)
    private Boolean deveTrocarSenha = true;

    @Column(name = "usuario_tentativas_falhas", nullable = false)
    private Integer tentativasFalhas = 0;

    @Column(name = "usuario_bloqueado_ate")
    private LocalDateTime bloqueadoAte;

    @Column(name = "usuario_ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "usuario_data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "usuario_data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public Usuario() {}

    @PrePersist
    protected void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        this.dataCriacao = agora;
        this.dataAtualizacao = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public String getNomeExibicao() { return nomeExibicao; }
    public void setNomeExibicao(String nomeExibicao) { this.nomeExibicao = nomeExibicao; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }
    public PerfilAcesso getPerfil() { return perfil; }
    public void setPerfil(PerfilAcesso perfil) { this.perfil = perfil; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public Boolean getDeveTrocarSenha() { return deveTrocarSenha; }
    public void setDeveTrocarSenha(Boolean deveTrocarSenha) { this.deveTrocarSenha = deveTrocarSenha; }
    public Integer getTentativasFalhas() { return tentativasFalhas; }
    public void setTentativasFalhas(Integer tentativasFalhas) { this.tentativasFalhas = tentativasFalhas; }
    public LocalDateTime getBloqueadoAte() { return bloqueadoAte; }
    public void setBloqueadoAte(LocalDateTime bloqueadoAte) { this.bloqueadoAte = bloqueadoAte; }
    public LocalDateTime getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(LocalDateTime ultimoLogin) { this.ultimoLogin = ultimoLogin; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }

    public boolean estaBloqueado() {
        return bloqueadoAte != null && bloqueadoAte.isAfter(LocalDateTime.now());
    }

    public boolean temPermissao(String chave) {
        return perfil != null && perfil.tem(chave);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
