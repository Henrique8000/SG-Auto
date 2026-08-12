package com.sgauto.app.util;

import com.sgauto.app.enums.PermissaoChave;
import com.sgauto.app.model.usuario.Usuario;
import com.sgauto.app.repository.usuario.PerfilAcessoRepository;
import org.springframework.stereotype.Component;

@Component
public class VerificaPermissaoUtil {

    private final PerfilAcessoRepository perfilAcessoRepository;

    public VerificaPermissaoUtil(PerfilAcessoRepository perfilAcessoRepository) {
        this.perfilAcessoRepository = perfilAcessoRepository;
    }

    public boolean verificar(PermissaoChave chave){
        Usuario usuarioAtual = SessaoUsuario.getInstancia().getUsuarioLogado();

        if (usuarioAtual == null || usuarioAtual.getPerfil() == null) {
            return false;
        }

        Long idDoPerfil = usuarioAtual.getPerfil().getId();

        return perfilAcessoRepository.perfilPossuiPermissao(idDoPerfil, chave.name());
    }
}