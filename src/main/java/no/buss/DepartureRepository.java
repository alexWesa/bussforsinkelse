package no.buss;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

public class DepartureRepository {
    private final Connection conn;

    public DepartureRepository(Connection conn) {
        this.conn = conn;
    }

    private static final String UPSERT = """
    INSERT INTO departure
        (service_journey_id, service_date, quay_id, line_code, destination,
         aimed_time, expected_time, realtime, cancelled)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT (service_journey_id, service_date, quay_id)
    DO UPDATE SET
        expected_time = EXCLUDED.expected_time,
        realtime      = EXCLUDED.realtime,
        cancelled     = EXCLUDED.cancelled,
        last_seen     = now()
    """;

    private void save(Departure dep) {
        String serviceJourneyID = dep.service_journeyID();
        LocalDate serviceDate = dep.service_date();
        String quay_id = dep.quay_id();
        String line_code = dep.line_code();
        String destination = dep.destination();
        OffsetDateTime aimed_time = dep.aimed_time();
        OffsetDateTime expected_time = dep.expected_time();
        Boolean realtime = dep.realtime();
        Boolean cancelled = dep.cancelled();
    }

    /**
    Gets the knownQuayIDs in database.
    Throws SQLException
     **/
    Set<String> knownQuayIDs() throws SQLException {
        Set<String> quayIDs = new HashSet<String> ();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT quay_id FROM quay")) {
                while (rs.next()) {
                    quayIDs.add(rs.getString("quay_id"));
                }
            }

        return quayIDs;
    }
}
