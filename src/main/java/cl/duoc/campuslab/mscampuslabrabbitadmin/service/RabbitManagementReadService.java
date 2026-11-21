package cl.duoc.campuslab.mscampuslabrabbitadmin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class RabbitManagementReadService {

    private final HttpClient client;
    private final String baseUrl;
    private final String authorization;

    public RabbitManagementReadService(
            @Value("${RABBITMQ_HOST}") String host,
            @Value("${RABBITMQ_USERNAME}") String username,
            @Value("${RABBITMQ_PASSWORD}") String password
    ) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        this.baseUrl = "http://" + host + ":15672/api/";

        String credentials = username + ":" + password;

        this.authorization = "Basic " +
                Base64.getEncoder().encodeToString(
                        credentials.getBytes(StandardCharsets.UTF_8)
                );
    }

    public String listQueues() {
        return getResource("queues");
    }

    public String listExchanges() {
        return getResource("exchanges");
    }

    public String listBindings() {
        return getResource("bindings");
    }

    private String getResource(String resource) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + resource))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", authorization)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "RabbitMQ Management API respondio HTTP "
                                + response.statusCode()
                );
            }

            return response.body();

        } catch (ResponseStatusException ex) {
            throw ex;

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Consulta RabbitMQ interrumpida"
            );

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No fue posible consultar RabbitMQ Management API"
            );
        }
    }
}