package com.lasystems.lagenda.controllers;


import com.lasystems.lagenda.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clienteService;

//    @GetMapping()
//    public ResponseEntity<List<ClientDto>> list() {
//        return ResponseEntity.ok(clienteService.list());
//    }

    @GetMapping("/{phone}")
    public ResponseEntity<?> getByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(clienteService.getByPhone(phone));
    }



}
