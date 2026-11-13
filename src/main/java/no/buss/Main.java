package no.buss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.Map;

import static java.time.LocalDate.now;


public class Main {
    public static void main(String[] args) throws Exception {

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

        repo.save(test);




        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT stop_id, name FROM stop_place")) {
            while (rs.next()) {
                System.out.println(rs.getString("stop_id") + " " + rs.getString("name"));
            }
        }
    }
}
