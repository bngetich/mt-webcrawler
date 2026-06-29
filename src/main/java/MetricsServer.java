import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class MetricsServer {
    private final HttpServer server;

    public MetricsServer(int port, Supplier<String> metricsSupplier) {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/metrics", exchange -> {
                String response = metricsSupplier.get();
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().add(
                        "Content-Type",
                        "text/plain; version=0.0.4"
                );

                exchange.sendResponseHeaders(200, bytes.length);

                try (OutputStream outputStream = exchange.getResponseBody()) {
                    outputStream.write(bytes);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException("Failed to start metrics server", e);
        }
    }

    public void start() {
        server.start();
        System.out.println("Metrics server running at http://localhost:9091/metrics");
    }

    public void stop() {
        server.stop(0);
    }
}