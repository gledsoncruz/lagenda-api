package com.lasystems.lagenda.controllers;


import com.lasystems.lagenda.dtos.CompanyDto;
import com.lasystems.lagenda.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> getById(@PathVariable String id) {
        Optional<CompanyDto> companyDto = companyService.getById(id);

        return ResponseEntity.ok(companyDto.get());
    }
}
