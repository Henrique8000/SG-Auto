package com.sgauto.app.service;

import com.sgauto.app.repository.ServicoRepository;
import org.springframework.stereotype.Service;

@Service
public class ServicoService {

    private final ServicoRepository servRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servRepository = servicoRepository;
    }


}
