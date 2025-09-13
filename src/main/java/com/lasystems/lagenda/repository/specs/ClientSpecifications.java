package com.lasystems.lagenda.repository.specs;

import com.lasystems.lagenda.models.Client;
import com.lasystems.lagenda.repository.filter.ClientFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;


public class ClientSpecifications {

    public static Specification<Client> usingFilter(ClientFilter filter) {
        return (root, query, builder) -> {
            ArrayList<Predicate> predicates = new ArrayList<Predicate>();

            if (filter.getId() != null) {
                predicates.add(builder.equal(root.get("id"), filter.getId()));
            }

            if (filter.getName() != null) {
                predicates.add(builder.like(builder.lower(root.get("name")), filter.getName()));
            }

            if (filter.getPhone() != null) {
                predicates.add(builder.like(builder.lower(root.get("phone")), filter.getPhone()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
