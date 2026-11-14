package no.buss;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.time.LocalDate.now;


public class Main {

    public static void main(String[] args) throws Exception {
        EnturClient entur = new EnturClient();
        List<String> stopIDs = List.of("NSR:StopPlace:31821", "NSR:StopPlace:31806");
        collectOne(entur, stopIDs);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(
                () -> {
                    try {
                        collectOne(entur, stopIDs);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 0, 30, TimeUnit.SECONDS);


        /*
        String url = "jdbc:postgresql://localhost:5432/bussforsinkelse";
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        Departure test = new Departure(
          "NSR:StopPlace:31821",
                now(),
                "NSR:Quay:54834",
                "6",
                "Birkelundstoppen",
                OffsetDateTime.now(),
                OffsetDateTime.now().plusMinutes(4),
                true,
                false
        );

        DepartureRepository repo = new DepartureRepository(DriverManager.getConnection(url, user, password));



        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT stop_id, name FROM stop_place")) {
            while (rs.next()) {
                System.out.println(rs.getString("stop_id") + " " + rs.getString("name"));
            }

         */
        }

    private static void collectOne(EnturClient entur, List<String> stopIDs) throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/bussforsinkelse";
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            DepartureRepository repo = new DepartureRepository(conn);
            Set<String> knownQuays = repo.knownQuayIDs();

            int saved = 0;
            for (String stopID : stopIDs) {
                for (Departure dep : entur.getDepartures(stopID)) {
                    if (knownQuays.contains(dep.quayID())) {
                        repo.save(dep);
                        saved ++;
                    }

                }
            }
            System.out.println(OffsetDateTime.now() + "Saved " + saved + " departures");

        } catch (Exception e) {
            System.err.println(OffsetDateTime.now() + "Error: " + e.getMessage());
        }
    }
}

