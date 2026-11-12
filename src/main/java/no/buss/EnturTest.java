package no.buss;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Map;

public class EnturTest {
    public static void main(String[] args) throws Exception {
        String query = """
        {
              stopPlace(id: "NSR:StopPlace:31821") {
              name
              estimatedCalls(numberOfDepartures: 10) {
              date
              realtime
              cancellation                             
              aimedDepartureTime                                        
              expectedDepartureTime
              quay { id publicCode }                                         
              destinationDisplay { frontText }
              serviceJourney { id }
              }
            }
         }
         """;

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(Map.of("query", query));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.entur.io/journey-planner/v3/graphql"))
                .timeout(Duration.ofMinutes(1))
                .header("Content-type", "application/json")
                .header("alexander-prosjekt", "alexander-bussforsinkelse")
                .POST(HttpRequest.BodyPublishers.ofString((body)))
                .build();

        HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());

                // System.out.println("Status: " + response.statusCode());
                // System.out.println(mapper.readTree(response.body()).toPrettyString());


        JsonNode root = mapper.readTree(response.body());
        JsonNode calls = root.path("data").path("stopPlace").path("estimatedCalls");

        for (JsonNode call : calls) {

            String aimed = call.path("aimedDepartureTime").asText();
            String expected = call.path("expectedDepartureTime").asText();
            String name = call.path("destinationDisplay").path("frontText").asText();
            String quay = call.path("quay").path("id").asText();
            String date = call.path("date").asText();

            OffsetDateTime aim = OffsetDateTime.parse(aimed);
            OffsetDateTime exp = OffsetDateTime.parse(expected);

            Long diffAimedTime = Duration.between(aim, exp).getSeconds();

            //System.out.println(diffAimedTime);

            System.out.println("Route: " + name +  " ID: " + quay + " Aimed depTime: " +
                    aim + " Expected depTime: " + exp + " Date: " + date);
        }

    }

}
