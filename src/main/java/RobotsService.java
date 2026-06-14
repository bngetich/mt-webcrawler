import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class RobotsService {
    private static final long ROBOTS_TTL_SECONDS = 24 * 60 * 60;

    private final RobotsCache robotsCache;
    private final HttpClient httpClient;

    public RobotsService(RobotsCache robotsCache) {
        this.robotsCache = robotsCache;
        this.httpClient = HttpClient.newHttpClient();
    }

    public boolean isAllowed(String url) {
        URI uri;

        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            return false;
        }

        String host = uri.getHost();

        if (host == null || host.isBlank()) {
            return false;
        }

        String path = uri.getPath();

        if (path == null || path.isBlank()) {
            path = "/";
        }

        RobotsRules rules = null;

        try {
            rules = robotsCache.get(host);
        } catch (Exception e) {
            // If the cache is unavailable, fetch rules directly and keep crawling.
        }

        if (rules == null) {
            rules = fetchAndParseRules(uri, host);

            try {
                robotsCache.put(host, rules, ROBOTS_TTL_SECONDS);
            } catch (Exception e) {
                // The fetched rules are still usable even if caching fails.
            }
        }

        return rules.isAllowed(path);
    }

    private RobotsRules fetchAndParseRules(URI uri, String host) {
        String scheme = uri.getScheme() == null ? "https" : uri.getScheme();
        String authority = uri.getAuthority();

        if (authority == null || authority.isBlank()) {
            return allowAllRules(host);
        }

        String robotsUrl = scheme + "://" + authority + "/robots.txt";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(robotsUrl))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return allowAllRules(host);
            }

            return parseRules(host, response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return allowAllRules(host);
        } catch (IOException e) {
            return allowAllRules(host);
        }
    }

    private RobotsRules parseRules(String host, String robotsTxt) {
        List<String> disallowedPaths = new ArrayList<>();
        Long crawlDelayMs = null;
        boolean appliesToUs = false;

        String[] lines = robotsTxt.split("\\R");

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isBlank() || trimmed.startsWith("#")) {
                continue;
            }

            int commentIndex = trimmed.indexOf("#");
            if (commentIndex >= 0) {
                trimmed = trimmed.substring(0, commentIndex).trim();
            }

            String lower = trimmed.toLowerCase();

            if (lower.startsWith("user-agent:")) {
                String agent = trimmed.substring("user-agent:".length()).trim();
                appliesToUs = agent.equals("*");
            } else if (appliesToUs && lower.startsWith("disallow:")) {
                String path = trimmed.substring("disallow:".length()).trim();

                if (!path.isBlank()) {
                    disallowedPaths.add(path);
                }
            } else if (appliesToUs && lower.startsWith("crawl-delay:")) {
                String value = trimmed.substring("crawl-delay:".length()).trim();

                try {
                    double seconds = Double.parseDouble(value);
                    crawlDelayMs = (long) (seconds * 1000);
                } catch (NumberFormatException e) {
                    // Ignore invalid crawl-delay values.
                }
            }
        }

        return new RobotsRules(
                host,
                disallowedPaths,
                crawlDelayMs,
                System.currentTimeMillis()
        );
    }

    private RobotsRules allowAllRules(String host) {
        return new RobotsRules(
                host,
                List.of(),
                null,
                System.currentTimeMillis()
        );
    }
}
