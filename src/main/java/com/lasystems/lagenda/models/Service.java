package com.lasystems.lagenda.models;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Table(name = "services")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper=false)
public class Service extends BaseModel {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "price")
    private Double price;
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Company company;
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    @Column(name = "requires_address")
    private Boolean requiresAddress;
    @ManyToMany(mappedBy = "services", fetch = FetchType.LAZY)
    private Set<Appointment> appointments = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "service_specialties",
            joinColumns = @JoinColumn(name = "service_id"),
            inverseJoinColumns = @JoinColumn(name = "specialty_id")
    )
    private Set<Specialist> specialties = new HashSet<>();

}
