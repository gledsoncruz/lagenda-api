package com.lasystems.lagenda.repository.specs;

import com.lasystems.lagenda.models.Client;
import com.lasystems.lagenda.models.Company;
import com.lasystems.lagenda.repository.filter.ClientFilter;
import com.lasystems.lagenda.repository.filter.CompanyFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

public class CompanySpec {

    public static Specification<Company> usingFilter(CompanyFilter filter) {
        return (root, query, builder) -> {
            ArrayList<Predicate> predicates = new ArrayList<Predicate>();

            if (filter.getId() != null) {
                predicates.add(builder.equal(root.get("id"), filter.getId()));
            }

            if (filter.getName() != null) {
                predicates.add(builder.like(builder.lower(root.get("name")), filter.getName()));
            }

            if (filter.getCategory() != null) {
                predicates.add(builder.like(builder.lower(root.get("category")), filter.getCategory()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
