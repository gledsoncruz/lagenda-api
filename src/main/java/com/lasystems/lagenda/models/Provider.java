package com.lasystems.lagenda.models;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Table(name = "providers")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper=false)
public class Provider extends BaseModel {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Company company;
    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;
    @Column(name = "phone")
    private String phone;
    @Column(name = "calendar_id")
    private String calendarId;
    @Column(name = "active")
    private Boolean active;
    @OneToMany(mappedBy = "provider")
    private List<Appointment> appointments;
//    @OneToMany(mappedBy = "provider")
//    private List<ProviderSpecialist> providerSpecialists;
    @ManyToMany
    @JoinTable(
            name = "provider_specialties",
            joinColumns = @JoinColumn(name = "provider_id"),
            inverseJoinColumns = @JoinColumn(name = "specialty_id")
    )
    private Set<Specialist> specialties = new HashSet<>();
}
