package com.parking.models;

import java.time.Instant;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ParkingTicket {
    private String ticketId;
    private Instant entryTime;
    private String status; // active, paid, expired
}
