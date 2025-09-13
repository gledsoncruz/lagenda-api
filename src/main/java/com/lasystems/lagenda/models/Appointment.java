package com.lasystems.lagenda.models;

import com.lasystems.lagenda.models.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "appointments")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper=false)
@Getter
@Setter
public class Appointment extends BaseModel {

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
    private Client client;
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Provider provider;
    @Column(name = "event_id")
    private String eventId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "text")
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
    @Column(name = "calendar_id")
    private String calendarId;
    @Column(name = "notes", columnDefinition = "text")
    private String notes;
    @Column(name = "start_appointment")
    private LocalDateTime start;
    @Column(name = "end_appointment")
    private LocalDateTime end;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "appointment_services",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"),
            foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT),
            inverseForeignKey = @ForeignKey(ConstraintMode.CONSTRAINT)
    )
    @Builder.Default
    private List<Service> services = new ArrayList<>();

    public void addService(Service service) {
        this.services.add(service);
        service.getAppointments().add(this);
    }

    public void removeService(Service service) {
        this.services.remove(service);
        service.getAppointments().remove(this);
    }

}
