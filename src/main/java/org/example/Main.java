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
import java.util.Comparator;
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

        //Create POJO from json => Konverterar JSON-svaret till en list av ElectricityPrice-objekt med Jackson
        ObjectMapper mapper = new ObjectMapper();

        List<ElectricityPrice> prices = mapper.readValue(
                response.body(),
                new TypeReference<List<ElectricityPrice>>() {
                }
        );

        //Kod för att få fram Meny & Interaktivitet
        String choice = "";
        while (!choice.equalsIgnoreCase("e")) {
            IO.println("Elpriser – Analysverktyg");
            IO.println("1. Visa elpriser");
            IO.println("2. Visa lägsta, högsta och medel pris");
            IO.println("3. Sortera priser (lägst till högst)");
            IO.println("4. Visa bästa laddningstid (4h sammanhängande)");
            IO.println("E. Avsluta");

            choice = IO.readln("Välj ett alternativ (1-4 eller E för att avsluta): ");

            //Felhantering vid ongiltig inmatning
            if (!choice.matches("[1-4eE]")) {
                IO.println("Ogiltigt alternativ! Försök igen.");
            }

            //Elprisanalys
            double minPrice = prices.get(0).SEK_per_kWh();
            double maxPrice = prices.get(0).SEK_per_kWh();
            double sum = 0;

            for (int i = 0; i < prices.size(); i++) {
                double price = prices.get(i).SEK_per_kWh();
                if (price < minPrice) {
                    minPrice = price;
                }
                if (price > maxPrice) {
                    maxPrice = price;
                }
                sum += price;
            }
            double averagePrice = sum / prices.size();
            switch (choice.toLowerCase()) {
                case "1" -> {
                    for (int i = 0; i < prices.size(); i++) {
                        IO.println(prices.get(i));
                    }
                }
                //Min, Max och Medelpris
                case "2" -> {
                    IO.println("Lägsta pris: %.2f öre/kWh".formatted(minPrice * 100));
                    IO.println("Högsta pris: %.2f öre/kWh".formatted(maxPrice * 100));
                    IO.println("Medelpris: %.2f öre/kWh".formatted(averagePrice * 100));
                }

                //Sortera priser (lägst till högst)
                case "3" ->  {prices.sort(Comparator.comparingDouble(ElectricityPrice::SEK_per_kWh));
                    IO.println( "Sortering av elpriser (lägst till högst) ");
                    for(ElectricityPrice price : prices) {
                        IO.println(
                                price.time_start().format(DateTimeFormatter.ofPattern("HH:mm"))
                                        + "-%.2f öre/kWh".formatted(price.SEK_per_kWh() * 100));
                    }
                }
                case "4" -> IO.println("Bästa laddningstid är inte implementerad ännu.");
                case "e" -> IO.println("Programmet avslutas");
                default -> IO.println("Ogiltigt val");

            }
        }
    }

    // Datamodell för ett elpris från api:et (objekt struktur beskrivning)
    record ElectricityPrice(double SEK_per_kWh,
                            double EUR_per_kWh,
                            double EXR,
                            OffsetDateTime time_start,
                            OffsetDateTime time_end) {
    }
}
