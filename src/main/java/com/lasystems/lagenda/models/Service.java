package com.lasystems.lagenda.models;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@Setter
@Table(name = "services")
@NoArgsConstructor
@AllArgsConstructor
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
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Company company;
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    @Column(name = "requires_address")
    private Boolean requiresAddress;
    // Service.java (apenas trechos relevantes)
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.Set<AppointmentService> appointmentServices = new java.util.HashSet<>();


    @ManyToMany
    @JoinTable(
            name = "service_specialties",
            joinColumns = @JoinColumn(name = "service_id"),
            inverseJoinColumns = @JoinColumn(name = "specialty_id")
    )
    private Set<Specialist> specialties = new HashSet<>();

}
