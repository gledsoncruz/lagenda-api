package com.lasystems.lagenda.models;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
public class BaseModel {

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
//    @LastModifiedDate
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//    @Version
//    @Column(name = "nu_version")
//    private Long version;
}
