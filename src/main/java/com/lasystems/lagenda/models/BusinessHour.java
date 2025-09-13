package com.lasystems.lagenda.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Table(name = "business_hours")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper=false)
public class BusinessHour extends BaseModel {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Company company;
    @Column(name = "day_of_week")
    @Check(constraints = "day_of_week >= 0 AND day_of_week <= 6")
    private Integer dayOfWeek;
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;


}
