package no.buss;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class EnturClient {

    private static final String URL = "https://api.entur.io/journey-planner/v3/graphql";
    private static final String QUERY = """
        {
              stopPlace(id: "%s") {
              name
              estimatedCalls(numberOfDepartures: 10) {
              date
              realtime
              cancellation                             
              aimedDepartureTime                                        
              expectedDepartureTime
              quay { id publicCode }                                         
              destinationDisplay { frontText }
              serviceJourney { id line { publicCode } }
              }
            }
         }
         """;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * creates a list of Departure-objects
     * @param stopID from where the list of Departures are generated
     * @return list of Departure-objects
     * @throws Exception
     */
    public List<Departure> getDepartures(String stopID) throws Exception {
        List<Departure> departures = new ArrayList<>();

        String query = QUERY.formatted(stopID);
        String body = mapper.writeValueAsString(Map.of("query", query));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .timeout(Duration.ofSeconds(15))
                .header("Content-type", "application/json")
                .header("ET-Client-Name", "alexander-bussforsinkelse")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Entur status " + response.statusCode() + ": " + response.body());
        }
        JsonNode root = mapper.readTree(response.body());
        if (root.has("errors")) {
            throw new IllegalStateException(
                    "GraphQL-error from Entur: " + root.path("errors").toString());
        }

        JsonNode stopPlace = root.path("data").path("stopPlace");
        if (stopPlace.isMissingNode() || stopPlace.isNull()) {
            throw new IllegalStateException("Could not find stopPlace: " + stopID);
        }
        JsonNode calls = stopPlace.path("estimatedCalls");

        for (JsonNode call : calls) {
            departures.add(createDeparture(call));
        }
        return departures;
    }

    /**
     * creates Departure-object
     * @param call JsonNode-object to be converted to Departure-object
     * @return Departure-object
     */
    private Departure createDeparture(JsonNode call) {
        String serviceJourneyID = call.path("serviceJourney").path("id").asText();
        LocalDate serviceDate = LocalDate.parse(call.path("date").asText());
        String quayID = call.path("quay").path("id").asText();
        String lineCode = call.path("serviceJourney").path("line").path("publicCode").asText();
        String destination = call.path("destinationDisplay").path("frontText").asText();
        OffsetDateTime aimed = OffsetDateTime.parse(call.path("aimedDepartureTime").asText());
        OffsetDateTime expected = OffsetDateTime.parse(call.path("expectedDepartureTime").asText());
        boolean realtime = call.path("realtime").asBoolean();
        boolean cancellation = call.path("cancellation").asBoolean();

        return (new Departure(serviceJourneyID, serviceDate, quayID, lineCode, destination, aimed,
                expected, realtime, cancellation));
    }
}
