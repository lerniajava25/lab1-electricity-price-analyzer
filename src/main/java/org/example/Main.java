package org.example;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

//Datanhämtning med Javas inbyggd HttpClient
public class Main {
    static void main() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_3)
                .build();

        // Anpassa URL:en till aktuellt datum istället för ett hårdkodat datum
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Stockholm"));

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy/MM-dd");

        String date = today.format(formatter);

        String url =
                "https://www.elprisetjustnu.se/api/v1/prices/"
                        + date
                        + "_SE3.json";
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .build();

        var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            IO.println("Fel vid hämtning av elpriser. HTTP-status: "
                    + response.statusCode());
            return;
        }
        IO.println("HTTP GET: " + response.body());

        //Create POJO from json => Konverterar JSON-svaret till en array av ElectricityPrice-objekt med Jackson
        ObjectMapper mapper = new ObjectMapper();

        List<ElectricityPrice> prices = mapper.readValue(
                response.body(),
                new TypeReference<List<ElectricityPrice>>() {}
        );
        System.out.println(prices);
    }

        // Datamodell för ett elpris från api:et (objekt struktur beskrivning)
        record ElectricityPrice(double SEK_per_kWh,
                                double EUR_per_kWh,
                                double EXR,
                                OffsetDateTime time_start,
                                OffsetDateTime time_end) {


    }
}
