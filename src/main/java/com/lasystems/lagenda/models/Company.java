package com.lasystems.lagenda.models;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Table(name = "companies")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper=false)
public class Company extends BaseModel {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;
    private String name;
    private String category;
    @OneToMany(mappedBy = "company")
    private List<Specialist> specialties;
    @OneToMany(mappedBy = "company")
    private List<Provider> providers;

}
