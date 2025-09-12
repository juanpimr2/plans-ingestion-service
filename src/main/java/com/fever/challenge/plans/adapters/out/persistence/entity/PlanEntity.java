package com.fever.challenge.plans.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="provider_id", unique = true, nullable = false)
    private String providerId;
    private String title;
    @Column(name="sell_mode", nullable = false)
    private String sellMode;
    @Column(name="starts_at", nullable = false)
    private Instant startsAt;
    @Column(name="ends_at",   nullable = false)
    private Instant endsAt;
    @Column(name="min_price")
    private Double minPrice;
    @Column(name="max_price")
    private Double maxPrice;
    @Column(name="first_seen_at", nullable = false)
    private Instant firstSeenAt;
    @Column(name="last_seen_at",  nullable = false)
    private Instant lastSeenAt;
    @Column(name="currently_available", nullable = false)
    private boolean currentlyAvailable;
}
