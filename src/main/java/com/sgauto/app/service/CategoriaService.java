package com.sgauto.app.service;

import com.sgauto.app.repository.CategoriaRepository;
import com.sgauto.app.repository.PecaRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoriaService {

    private final CategoriaRepository catRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.catRepository = categoriaRepository;
    }




}
