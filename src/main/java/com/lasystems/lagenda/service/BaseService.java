package com.lasystems.lagenda.service;

import java.util.List;

public interface BaseService<M, F> {

    List<M> list(F filter);
    M buscarPorId(Long id);
    M salvar(M obj);
    M alterar(M obj);
    void delete(Long id);
}
