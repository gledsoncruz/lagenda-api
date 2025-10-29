package com.lasystems.lagenda.models;

import com.lasystems.lagenda.models.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    @Column(name = "start_appointment", nullable = false)
    private LocalDateTime start;
    @Column(name = "end_appointment", nullable = false)
    private LocalDateTime end;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<AppointmentService> appointmentServices = new HashSet<>();

    public void addService(Service service) {
        var linkId = new AppointmentServiceId(this.id, service.getId(), this.company.getId());
        var link = AppointmentService.builder()
                .id(linkId)
                .appointment(this)
                .service(service)
                .priceService(service.getPrice())
                .build();
        appointmentServices.add(link);
        service.getAppointmentServices().add(link);
    }

    public void removeService(Service service) {
        var key = new AppointmentServiceId(this.id, service.getId(), this.company.getId());
        appointmentServices.removeIf(l -> l.getId().equals(key));
        service.getAppointmentServices().removeIf(l -> l.getId().equals(key));
    }

    public void updateServices(Set<AppointmentService> newServices) {
        this.appointmentServices.clear();
        this.appointmentServices.addAll(newServices);
    }

    /**
     * Método helper para obter start com timezone explícito.
     * Útil para logs e debugging.
     */
    @Transient
    public ZonedDateTime getStartWithZone() {
        return start.atZone(ZoneId.of("America/Sao_Paulo"));
    }

    /**
     * Método helper para obter end com timezone explícito.
     */
    @Transient
    public ZonedDateTime getEndWithZone() {
        return end.atZone(ZoneId.of("America/Sao_Paulo"));
    }

}
