package ru.strelchm.scheduler_perf.comparison;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class MetricsServer {

    private static final int DEFAULT_PORT = 8085;

    private final int port;
    private final PrometheusMeterRegistry meterRegistry;
    private final ExecutorService executor;
    private com.sun.net.httpserver.HttpServer server;

    public MetricsServer(PrometheusMeterRegistry meterRegistry) {
        this(meterRegistry, DEFAULT_PORT);
    }

    public MetricsServer(PrometheusMeterRegistry meterRegistry, int port) {
        this.meterRegistry = meterRegistry;
        this.port = port;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void start() {
        executor.submit(() -> {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create HTTP server", e);
            }

            server.createContext("/actuator/metrics", exchange -> {
                String response = meterRegistry.scrape();
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            });

            server.setExecutor(executor);
            server.start();
            log.info("Metrics HTTP server started on port {}", port);
        });
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            log.info("Metrics HTTP server stopped");
        }
        executor.shutdownNow();
    }
}
