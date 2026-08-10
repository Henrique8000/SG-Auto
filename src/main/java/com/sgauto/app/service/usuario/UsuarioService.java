package com.sgauto.app.service.usuario;

import com.sgauto.app.model.Funcionario;
import com.sgauto.app.model.usuario.PerfilAcesso;
import com.sgauto.app.model.usuario.Usuario;
import com.sgauto.app.repository.FuncionarioRepository;
import com.sgauto.app.repository.usuario.PerfilAcessoRepository;
import com.sgauto.app.repository.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService {

    private static final int MAX_TENTATIVAS = 5;
    private static final int MINUTOS_BLOQUEIO = 15;
    private static final int TAMANHO_MINIMO_SENHA = 6;

    private final UsuarioRepository usuarioRepository;
    private final PerfilAcessoRepository perfilAcessoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PerfilAcessoRepository perfilAcessoRepository,
                          FuncionarioRepository funcionarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilAcessoRepository = perfilAcessoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarAtivos() {
        return usuarioRepository.findByAtivoTrue();
    }

    @Transactional
    public Usuario cadastrar(String login, String senhaTemporaria, String nomeExibicao,
                             String email, Long funcionarioId, Long perfilId) {
        validarLogin(login, null);
        validarSenha(senhaTemporaria);

        PerfilAcesso perfil = buscarPerfilOuFalhar(perfilId);

        Usuario usuario = new Usuario();
        usuario.setLogin(login.trim().toLowerCase());
        usuario.setSenhaHash(passwordEncoder.encode(senhaTemporaria));
        usuario.setNomeExibicao(nomeExibicao.trim());
        usuario.setEmail(email);
        usuario.setPerfil(perfil);
        usuario.setDeveTrocarSenha(true);

        if (funcionarioId != null) {
            Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                    .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado: " + funcionarioId));
            usuario.setFuncionario(funcionario);
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Long id, String nomeExibicao, String email, Long funcionarioId, Long perfilId) {
        Usuario usuario = buscarOuFalhar(id);

        PerfilAcesso perfil = buscarPerfilOuFalhar(perfilId);

        usuario.setNomeExibicao(nomeExibicao.trim());
        usuario.setEmail(email);
        usuario.setPerfil(perfil);

        if (funcionarioId != null) {
            Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                    .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado: " + funcionarioId));
            usuario.setFuncionario(funcionario);
        } else {
            usuario.setFuncionario(null);
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario autenticar(String login, String senhaDigitada) {
        Usuario usuario = usuarioRepository.findByLogin(login.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Usuário ou senha inválidos."));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new IllegalStateException("Este usuário está inativo. Procure um administrador.");
        }

        if (usuario.estaBloqueado()) {
            throw new IllegalStateException("Usuário bloqueado temporariamente por excesso de tentativas. Tente novamente mais tarde.");
        }

        boolean senhaCorreta = passwordEncoder.matches(senhaDigitada, usuario.getSenhaHash());

        if (!senhaCorreta) {
            registrarTentativaFalha(usuario);
            throw new IllegalArgumentException("Usuário ou senha inválidos.");
        }

        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuario.setUltimoLogin(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    private void registrarTentativaFalha(Usuario usuario) {
        int tentativas = usuario.getTentativasFalhas() + 1;
        usuario.setTentativasFalhas(tentativas);

        if (tentativas >= MAX_TENTATIVAS) {
            usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEIO));
        }

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void trocarSenha(Long id, String senhaAtual, String novaSenha) {
        Usuario usuario = buscarOuFalhar(id);

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new IllegalArgumentException("Senha atual incorreta.");
        }

        validarSenha(novaSenha);

        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setDeveTrocarSenha(false);

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void redefinirSenha(Long id, String senhaTemporaria) {
        Usuario usuario = buscarOuFalhar(id);

        validarSenha(senhaTemporaria);

        usuario.setSenhaHash(passwordEncoder.encode(senhaTemporaria));
        usuario.setDeveTrocarSenha(true);
        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void ativar(Long id) {
        buscarOuFalhar(id).setAtivo(true);
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = buscarOuFalhar(id);

        if (Boolean.TRUE.equals(usuario.getPerfil().getProtegido())
                && usuarioRepository.countByPerfilIdAndAtivoTrue(usuario.getPerfil().getId()) <= 1) {
            throw new IllegalStateException("Não é possível desativar o último usuário com perfil Administrador ativo.");
        }

        usuario.setAtivo(false);
    }

    @Transactional(readOnly = true)
    public boolean possuiPermissao(Long usuarioId, String chavePermissao) {
        return buscarOuFalhar(usuarioId).temPermissao(chavePermissao);
    }

    private void validarLogin(String login, Long idAtual) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("O login é obrigatório.");
        }
        String loginNormalizado = login.trim().toLowerCase();
        boolean duplicado = (idAtual == null)
                ? usuarioRepository.existsByLogin(loginNormalizado)
                : usuarioRepository.existsByLoginAndIdNot(loginNormalizado, idAtual);
        if (duplicado) {
            throw new IllegalArgumentException("Já existe um usuário com este login.");
        }
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException("A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
    }

    private PerfilAcesso buscarPerfilOuFalhar(Long perfilId) {
        return perfilAcessoRepository.findById(perfilId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil não encontrado: " + perfilId));
    }

    private Usuario buscarOuFalhar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));
    }
}
