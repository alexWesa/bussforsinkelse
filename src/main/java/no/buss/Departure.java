package no.buss;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record Departure(
        String serviceJourneyID,
        LocalDate service_date,
        String quay_id,
        String line_code,
        String destination,
        OffsetDateTime aimed_time,
        OffsetDateTime expected_time,
        Boolean realtime,
        Boolean cancelled
) {
}
