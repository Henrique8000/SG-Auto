package com.sgauto.app.util;

import com.sgauto.app.service.CaixaService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CaixaInicializador implements ApplicationRunner {

    private final CaixaService caixaService;

    public CaixaInicializador(CaixaService caixaService) {
        this.caixaService = caixaService;
    }

    @Override
    public void run(ApplicationArguments args) {
        caixaService.garantirCaixaAberto();
    }
}