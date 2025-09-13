package com.lasystems.lagenda.service;

import com.lasystems.lagenda.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository repo;

    public List<com.lasystems.lagenda.models.Service> findAllById(List<String> ids) {
        return repo.findAllById(ids.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList()));
    }


}
