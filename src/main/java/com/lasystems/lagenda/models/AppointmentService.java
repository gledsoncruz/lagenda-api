package com.lasystems.lagenda.models;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "appointment_services", schema = "public")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppointmentService {

    @EmbeddedId
    private AppointmentServiceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("appointmentId")
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("serviceId")
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("companyId")
//    @JoinColumn(name = "company_id", nullable = false)
//    private Company company;
    @Column(name = "price_service", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceService;

    public java.util.UUID getCompanyId() {
        return id != null ? id.getCompanyId() : null;
    }

}
