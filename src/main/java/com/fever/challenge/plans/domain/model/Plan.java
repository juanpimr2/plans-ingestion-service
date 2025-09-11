package com.fever.challenge.plans.domain.model;

import java.time.Instant;
import java.util.List;
import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Plan {
    private String providerId;
    private String title;
    private String sellMode;
    private Instant startsAt;
    private Instant endsAt;
    private List<Zone> zones;
    private Instant firstSeenAt;
    private Instant lastSeenAt;
    private boolean currentlyAvailable;
}
