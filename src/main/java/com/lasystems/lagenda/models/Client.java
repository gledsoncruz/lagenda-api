package com.lasystems.lagenda.models;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Table(name = "clients")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper=false)
public class Client extends BaseModel {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Company company;
    @Column(name = "name", columnDefinition = "text")
    private String name;
    @Column(name = "email", columnDefinition = "text")
    private String email;
    @Column(name = "phone", columnDefinition = "text")
    private String phone;
    @OneToMany(mappedBy = "client")
    @SQLRestriction("end_appointment >= (current_timestamp AT TIME ZONE 'UTC')::date")
    private List<Appointment> appointments = new ArrayList<>();
    @Column(columnDefinition = "jsonb", name = "conversation_history")
    private String conversationHistory;

}
