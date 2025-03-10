package com.parking.models;

import java.time.Instant;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class Payment {
    private String ticketId;
    private Double amount;
    private Instant timestamp;
    private String status; // pending, processed, failed
}
