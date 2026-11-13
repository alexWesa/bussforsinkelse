package no.buss;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record Departure(
        String serviceJourneyID,
        LocalDate serviceDate,
        String quayID,
        String lineCode,
        String destination,
        OffsetDateTime aimedTime,
        OffsetDateTime expectedTime,
        boolean realtime,
        boolean cancelled
) {
}
