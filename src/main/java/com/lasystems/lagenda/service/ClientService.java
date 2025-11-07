package com.lasystems.lagenda.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lasystems.lagenda.dtos.ClientDto;
import com.lasystems.lagenda.exceptions.EntityNotFoundException;
import com.lasystems.lagenda.exceptions.UUIDIllegalArgumentException;
import com.lasystems.lagenda.models.Client;
import com.lasystems.lagenda.models.enums.AppointmentStatus;
import com.lasystems.lagenda.repository.ClientRepository;
import com.lasystems.lagenda.repository.filter.ClientFilter;
import com.lasystems.lagenda.repository.specs.ClientSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository repo;

    private final ObjectMapper objectMapper = new ObjectMapper();


//    public List<ClientDto> list() {
//        return repo.findClientDto();
//    }

    public Optional<JsonNode> getByPhone(String phone) {
        return repo.findByPhone(phone)
                .map(client -> {
                    ObjectNode node = objectMapper.createObjectNode();
                    node.put("id", client.getId().toString());
                    node.put("name", client.getName());
                    node.put("email", client.getEmail());
                    node.put("phone", client.getPhone());
                    node.put("attendantHuman", client.getAttendantHuman());

                    // Adiciona company
//                    ObjectNode companyNode = objectMapper.createObjectNode();
//                    companyNode.put("id", client.getCompany().getId().toString());
//                    companyNode.put("name", client.getCompany().getName());
//                    companyNode.put("category", client.getCompany().getCategory());
//                    node.set("company", companyNode);

                    // Adiciona appointments
                    ArrayNode appointmentsNode = node.putArray("appointments");

                    client.getAppointments().stream().filter(f ->
                            !f.getStatus().equals(AppointmentStatus.CANCELLED) && !f.getStatus().equals(AppointmentStatus.COMPLETED)).forEach(a -> {
                        ObjectNode appNode = appointmentsNode.addObject();
                        appNode.put("id", a.getId().toString());
                        appNode.put("start", a.getStart().toString());
                        appNode.put("end", a.getEnd().toString());
                        appNode.put("status", a.getStatus().name());


                        ArrayNode serviceArrayNode = appNode.putArray("services");


                        a.getAppointmentServices().forEach(s -> {
                            ObjectNode serviceNode = objectMapper.createObjectNode();
                            serviceNode.put("id", s.getId().toString());
                            serviceNode.put("name", s.getService().getName());
                            serviceNode.put("price", s.getService().getPrice().toString());
                            serviceArrayNode.add(serviceNode);
                        });

                    });

                    // Adiciona conversationHistory (já é JSON)
                    try {
                        JsonNode history = client.getConversationHistory() != null && !client.getConversationHistory().trim().isEmpty()
                                ? objectMapper.readTree(client.getConversationHistory())
                                : objectMapper.createArrayNode();
                        node.set("conversationHistory", history);
                    } catch (Exception e) {
                        node.set("conversationHistory", objectMapper.createArrayNode());
                    }

                    return (JsonNode) node;
                });
    }

    public Client findById(String id) {
        try {
            Optional<Client> client = repo.findById(UUID.fromString(id));
            if (client.isEmpty()) {
                throw new EntityNotFoundException();
            }
            return client.get();
        } catch(IllegalArgumentException ex) {
            throw new UUIDIllegalArgumentException();
        }
    }


}
