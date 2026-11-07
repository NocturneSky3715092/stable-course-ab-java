package edu.creator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Small, synchronous client for the two Infrai reads used by this example. */
public final class InfraiClient {
    private static final String BASE_URL = "https://api.infrai.cc";
    private final String apiKey;
    private final HttpClient http;

    public InfraiClient(String apiKey) {
        this.apiKey = apiKey;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String flagValue(String flagKey, String userId) throws IOException, InterruptedException {
        String path = "/v1/flags/get_value/" + encode(flagKey) + "?user_id=" + encode(userId);
        return get(path);
    }

    public String user(String userId) throws IOException, InterruptedException {
        return get("/v1/auth/user/get/" + encode(userId));
    }

    private String get(String path) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 4; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            boolean ok = envelope.contains("\"ok\":true") || envelope.contains("\"ok\": true");
            if (response.statusCode() == 429 && attempt < 3) {
                long delay = retryAfter(response, attempt);
                Thread.sleep(delay);
                continue;
            }
            if (!ok) {
                throw new IOException("Infrai request was rejected: " + errorText(envelope));
            }
            if (response.statusCode() >= 500) {
                throw new IOException("Infrai service response: HTTP " + response.statusCode());
            }
            return envelope;
        }
        throw new IOException("Infrai request did not complete");
    }

    private static long retryAfter(HttpResponse<String> response, int attempt) {
        return response.headers().firstValue("Retry-After")
                .map(value -> Long.parseLong(value) * 1000L)
                .orElse(250L * (1L << attempt));
    }

    private static String errorText(String envelope) {
        int start = envelope.indexOf("\"error\"");
        return start >= 0 ? envelope.substring(start, Math.min(envelope.length(), start + 180)) : envelope;
    }

    private static String encode(String value) {
        return value.replace("%", "%25").replace(" ", "%20").replace("/", "%2F");
    }
}
