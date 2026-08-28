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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;


public class Main {

    static void main() throws IOException, InterruptedException {
        showMenu();
    }

    //1.1 API-Integration
    // Datanhämtning med Javas inbyggd HttpClient
    static List<ElectricityPrice> showApiIntegration(String selectedArea)
            throws IOException, InterruptedException {

        HttpClient httpClient = HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_3)
                .build();

        // Anpassa URL:en till aktuellt datum enligt svensk tidszon
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Stockholm"));

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy/MM-dd");

        String date = today.format(formatter);

        String url =
                "https://www.elprisetjustnu.se/api/v1/prices/"
                        + date
                        + "_"
                        + selectedArea
                        + ".json";
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .build();

        var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            IO.println("Fel vid hämtning av elpriser. HTTP-status: "
                    + response.statusCode());
            return new ArrayList<>();
        }

        //1.2 Create POJO from json => Konverterar JSON-svaret till en list av ElectricityPrice-objekt med Jackson
        ObjectMapper mapper = new ObjectMapper();

        List<ElectricityPrice> prices = mapper.readValue(
                response.body(),
                new TypeReference<List<ElectricityPrice>>() {
                }
        );
        return prices;
    }

    // 1. Visa hämtade elpriser (Case-1)
    static void showPrices(List<ElectricityPrice> prices) {
        for (ElectricityPrice price : prices) {
            IO.println(price);
        }
    }

    //Meny & Interaktivitet
    static void showMenu() throws IOException, InterruptedException {

        Scanner scanner = new Scanner(System.in);

        String selectedArea = null;
        List<ElectricityPrice> prices = new ArrayList<>();

        while (true) {
            IO.println();
            IO.println("Elpriser – Analysverktyg");
            IO.println("========================");

            showSelectedArea(selectedArea);

            IO.println("1. Välj elområde (SE1, SE2, SE3, SE4)");
            IO.println("2. Min, Max och Medelpris");
            IO.println("3. Sortera priser (lägst till högst)");
            IO.println("4. Bästa laddningstid (4h sammanhängande)");
            IO.println("e. Avsluta");

            String choice = scanner.nextLine();
            switch (choice.toLowerCase()) {
                case "1":
                    IO.println("Välj elområde: SE1, SE2, SE3 eller SE4");
                    String area = scanner.nextLine().toUpperCase();

                    if (isValidArea(area)) {
                        selectedArea = area;
                        prices = showApiIntegration(selectedArea);

                        IO.println("Elområde ändrat till " + selectedArea);
                        showPrices(prices);
                    } else {
                        IO.println("Ogiltigt elområde.");
                    }
                    break;

                case "2", "3", "4":
                    handlePriceChoice(choice, prices);
                    break;

                case "e":
                    IO.println("Programmet avslutas.");
                    return;

                default:
                    IO.println("Ogiltigt val. Försök igen.");
            }
        }
    }

    static void showSelectedArea(String selectedArea) {
        if (selectedArea == null) {
            IO.println("Valt elområde: Inget område valt");
        } else {
            IO.println("Valt elområde: " + selectedArea);
        }
    }

    static boolean hasPrices(List<ElectricityPrice> prices) {
        if (prices.isEmpty()) {
            IO.println("Välj först ett elområde.");
            return false;
        }
        return true;
    }

    static void handlePriceChoice(String choice, List<ElectricityPrice> prices) {
        if (!hasPrices(prices)) {
            return;
        }
        switch (choice) {
            case "2" -> showPriceAnalysis(prices);
            case "3" -> sortedPrices(prices);
            case "4" -> showBestChargingTime(prices);
            default -> IO.println("Ogiltigt val.");
        }
    }
    static boolean isValidArea(String area) {
        return area.equals("SE1")
                || area.equals("SE2")
                || area.equals("SE3")
                || area.equals("SE4");
    }
        //2. Elprisanalys (case-2)
        static void showPriceAnalysis(
                List<ElectricityPrice> prices) {

            if (prices.isEmpty()) {
                IO.println("Inga elpriser att analysera.");
                return;
            }
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


            //Min, Max och Medelpris
            IO.println("Lägsta pris: %.2f öre/kWh".formatted(minPrice * 100));
            IO.println("Högsta pris: %.2f öre/kWh".formatted(maxPrice * 100));
            IO.println("Medelpris: %.2f öre/kWh".formatted(averagePrice * 100));
        }

            //3. Sortera priser/case-3 (lägst till högst), genom att först skapa en kopia "sortedPrices"

         static void sortedPrices(List<ElectricityPrice> prices) {
                    List< ElectricityPrice > sortedPrices = new ArrayList<>(prices);
            sortedPrices.sort(
                    Comparator.comparingDouble(ElectricityPrice::SEK_per_kWh)
            );
            IO.println("Sortering av elpriser (lägst till högst) ");
            for (ElectricityPrice price : sortedPrices) {
                IO.println(
                        price.time_start().format(DateTimeFormatter.ofPattern("HH:mm"))
                                + " --> %.2f öre/kWh".formatted(price.SEK_per_kWh() * 100));
            }
        }

        // 4. Sliding Window/case-4
        static void showBestChargingTime(List<ElectricityPrice> prices) {
            // API-priserna är i 15 minutersintervall. 4 h = 16 x 15 min.
            int windowSize = 16;

            //Att hantera listor som är kortare än fyra timmar
            if (prices.size() < windowSize) {
                IO.println("Det finns inte tillräckligt med priser för att beräkna 4 timmar.");
                return;
            }
            double currentSum = 0;

            // Första 4-timmarsperioden
            for (int i = 0; i < windowSize; i++) {
                currentSum += prices.get(i).SEK_per_kWh();
            }
            double lowestSum = currentSum;
            int bestStartIndex = 0;

            // Flytta fönstret 15 minuter i taget
            for (int i = windowSize; i < prices.size(); i++) {

                currentSum -= prices.get(i - windowSize).SEK_per_kWh();
                currentSum += prices.get(i).SEK_per_kWh();

                if (currentSum < lowestSum) {
                    lowestSum = currentSum;
                    bestStartIndex = i - windowSize + 1;
                }
            }

            ElectricityPrice startPrice = prices.get(bestStartIndex);
            ElectricityPrice endPrice = prices.get(bestStartIndex + windowSize - 1);

            double averagePrice = lowestSum / windowSize;

            IO.println(
                    "Bästa laddningstid: "
                            + startPrice.time_start().format(DateTimeFormatter.ofPattern("HH:mm"))
                            + " - "
                            + endPrice.time_end().format(DateTimeFormatter.ofPattern("HH:mm")));

            IO.println(
                    "Medelpris: %.2f öre/kWh"
                            .formatted(averagePrice * 100));
        }
        // Datamodell för ett elpris från api:et (objekt struktur beskrivning)
        record ElectricityPrice(double SEK_per_kWh,
                                double EUR_per_kWh,
                                double EXR,
                                OffsetDateTime time_start,
                                OffsetDateTime time_end) {
        }
    }
