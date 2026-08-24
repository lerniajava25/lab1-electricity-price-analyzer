package org.example;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

//Datanhämtning med Javas inbyggd HttpClient
public class Main {
    static void main() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient
                .newBuilder()
                .version(HttpClient.Version.HTTP_3)
                .build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://www.elprisetjustnu.se/api/v1/prices/2026/08-23_SE3.json"))
                .build();

        var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        IO.println("HTTP GET: " + response.body());


        // Datamodell för ett elpris från api:et (objekt struktur beskrivning)
        record ElectricityPrice(double SEK_per_kWh,
                                double EUR_per_kWh,
                                double EXR,
                                String time_start,
                                String time_end) {

        }
    }
}
