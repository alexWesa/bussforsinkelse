package no.buss;

import java.sql.*;
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

    public void save(Departure dep) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPSERT)) {
            ps.setString(1, dep.serviceJourneyID());
            ps.setObject(2, dep.serviceDate());
            ps.setString(3, dep.quayID());
            ps.setString(4, dep.lineCode());
            ps.setString(5, dep.destination());
            ps.setObject(6, dep.aimedTime());
            ps.setObject(7, dep.expectedTime());
            ps.setBoolean(8, dep.realtime());
            ps.setBoolean(9, dep.cancelled());

            ps.executeUpdate();
        }
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
