package com.lasystems.lagenda.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Table(name = "service_specialties")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper=false)
public class ServiceSpecialties extends BaseModel {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Company company;
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Specialist specialist;
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Service service;

}
