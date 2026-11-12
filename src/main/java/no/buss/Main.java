package no.buss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;



public class Main {
    public static void main(String[] args) throws Exception {

        // Test 1: Jackson
        ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> test = mapper.readValue("{\"hei\":\"verden\"}", Map.class);
        System.out.println("Jackson virker: " + test);

        // Test 2: database
        String url = "jdbc:postgresql://localhost:5432/bussforsinkelse";
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT stop_id, name FROM stop_place")) {
            while (rs.next()) {
                System.out.println(rs.getString("stop_id") + " " + rs.getString("name"));
            }
        }
    }
}
