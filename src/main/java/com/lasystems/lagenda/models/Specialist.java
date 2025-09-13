package com.lasystems.lagenda.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Table(name = "specialties")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper=false)
public class Specialist extends BaseModel {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;
    @Column(name = "name", nullable = false)
    private String name;
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Company company;
    @Column(name = "description", nullable = false)
    private String description;

}
