package com.sgauto.app.util;

import com.sgauto.app.model.usuario.Usuario;

public class SessaoUsuario {
    private static SessaoUsuario instancia;
    private Usuario usuarioLogado;

    private SessaoUsuario() {}

    public static SessaoUsuario getInstancia() {
        if (instancia == null) {
            instancia = new SessaoUsuario();
        }
        return instancia;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public void setUsuarioLogado(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
    }

    public void limparSessao() {
        this.usuarioLogado = null;
    }
}
