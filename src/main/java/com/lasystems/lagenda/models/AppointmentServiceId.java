package com.lasystems.lagenda.models;

import lombok.*;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AppointmentServiceId implements Serializable {
    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;
}

