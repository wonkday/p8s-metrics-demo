package com.wonkday.demo.metrics;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
//import io.prometheus.metrics.exporter.httpserver.MetricsHandler;
//import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import io.prometheus.metrics.model.snapshots.Unit;
import java.io.IOException;

/**
 * Simple example of an application exposing metrics via Prometheus' built-in HTTPServer.
 * Based on P8s Example -
 * https://github.com/prometheus/client_java/blob/main/examples/example-exporter-servlet-tomcat/src/main/java/io/prometheus/metrics/examples/tomcat_servlet/HelloWorldServlet.java
 */
public class MetricsDemo {

    private static final int SLEEP_TIME_IN_SECONDS = 20 * 1000;
    private static final int PORT_NUM = 9400;
    
    // Configuration with defaults
    private static final String DEFAULT_NAMESPACE = "default";
    private static final String DEFAULT_APP_NAME = "metrics-demo";
    private static final String DEFAULT_URI_BASE = "/domain/demo-api";
    
    // Default percentages (should sum to 100)
    private static final int DEFAULT_STATUS_200_PERCENT = 50;
    private static final int DEFAULT_STATUS_201_PERCENT = 20;
    private static final int DEFAULT_STATUS_401_PERCENT = 10;
    private static final int DEFAULT_STATUS_404_PERCENT = 10;
    private static final int DEFAULT_STATUS_500_PERCENT = 10;

    /**
     * Gets configuration value from environment variable or returns default.
     */
    private static String getConfig(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Gets integer configuration value from environment variable or returns default.
     */
    private static int getConfigAsInt(String envVar, int defaultValue) {
        String value = System.getenv(envVar);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid value for " + envVar + ": " + value + ". Using default: " + defaultValue);
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Determines HTTP status code based on configured percentages.
     * Returns a status code string (e.g., "200", "201", "401", "404", "500").
     */
    private static String determineStatusCode(int status200Percent, int status201Percent, 
                                              int status401Percent, int status404Percent, 
                                              int status500Percent) {

        // Generate a random number between 0 and 100                                        
        int random = (int) (Math.random() * 100);
        
        // Cumulative ranges based on percentages
        int range200 = status200Percent;
        int range201 = range200 + status201Percent;
        int range401 = range201 + status401Percent;
        int range404 = range401 + status404Percent;
        // range500 = range404 + status500Percent (should be 100)
        
        if (random < range200) {
            return "200";
        } else if (random < range201) {
            return "201";
        } else if (random < range401) {
            return "401";
        } else if (random < range404) {
            return "404";
        } else {
            return "500";
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        // Load configuration from environment variables (from ConfigMap)
        String namespace = getConfig("namespace", DEFAULT_NAMESPACE);
        String appName = getConfig("appName", DEFAULT_APP_NAME);
        String uriBase = getConfig("uriBase", DEFAULT_URI_BASE);

        long sleepTimeInMillis = getConfigAsInt("sleepTimeInMillis", SLEEP_TIME_IN_SECONDS);

        int status200Percent = getConfigAsInt("status_200_percent", DEFAULT_STATUS_200_PERCENT);
        int status201Percent = getConfigAsInt("status_201_percent", DEFAULT_STATUS_201_PERCENT);
        int status401Percent = getConfigAsInt("status_401_percent", DEFAULT_STATUS_401_PERCENT);
        int status404Percent = getConfigAsInt("status_404_percent", DEFAULT_STATUS_404_PERCENT);
        int status500Percent = getConfigAsInt("status_500_percent", DEFAULT_STATUS_500_PERCENT);
        
        System.out.println("Configuration loaded:");
        System.out.println("  Namespace: " + namespace);
        System.out.println("  App Name: " + appName);
        System.out.println("  URI Base: " + uriBase);
        System.out.println("  Sleep Time: " + sleepTimeInMillis);
        System.out.println("  Status code percentages - \n" +
                    "    [ 200: " + status200Percent + "% ],\n" +
                    "    [ 201: " + status201Percent + "% ],\n" +
                    "    [ 401: " + status401Percent + "% ],\n" +
                    "    [ 404: " + status404Percent + "% ],\n" +
                    "    [ 500: " + status500Percent + "% ]");

        // Uncomment if we need to scrape JVM metrics as well
        //JvmMetrics.builder().register();

        // Note: 
        // uptime_seconds_total is not a great example because:
        // The built-in JvmMetrics have an out-of-the-box metric named process_start_time_seconds
        // with the start timestamp in seconds, so we need to know the uptime then we can simply
        // run the Prometheus query
        //     time() - process_start_time_seconds
        // rather than creating a custom uptime metric.
        // This metric was created just to demonstrate custom counter metric usage.
        Counter uptimeCounter =
                Counter.builder()
                        .name("uptime_seconds_total")
                        .help("total number of seconds since this application was started")
                        .unit(Unit.SECONDS)
                        .register();

        Counter requestCounter =
                Counter.builder()
                        .name("requests_total")
                        .help("total number of requests")
                        .labelNames("app")
                        .register();

        Histogram requestDuration = Histogram.builder()
                .name("http_server_requests_seconds")
                .help("Duration of HTTP requests in seconds")
                .labelNames("namespace", "app", "method", "uri", "status")
                .register();


        HTTPServer server = HTTPServer.builder().port(PORT_NUM).buildAndStart();
        System.out.println(
                "HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");


        while (true) {
            Thread.sleep(sleepTimeInMillis);
            uptimeCounter.inc();
            requestCounter.labelValues(appName).inc();

            long start = System.nanoTime();
            // Simulate request handling
            long randomSleepTimeForRequestSimulation = (long) (Math.random() * 500);
            //System.out.println("sleepTime:"+randomSleepTimeForRequestSimulation);
            Thread.sleep(randomSleepTimeForRequestSimulation);

            double durationSeconds = (System.nanoTime() - start) / 1e9;

            // Determine status code based on configured percentages
            String statusCode = determineStatusCode(status200Percent, status201Percent, 
                                                    status401Percent, status404Percent, status500Percent);
            
            // Determine HTTP method randomly (GET or POST)
            String method = (Math.random() < 0.6) ? "GET" : "POST";
            
            // Determine path based on method
            String uriPath;
            if ("GET".equals(method)) {
                // GET requests use the URI base with an ID parameter
                uriPath = uriBase + "/{id}";
            } else {
                // POST requests use the URI base directly
                uriPath = uriBase;
            }

            // Record the duration with configurable values
            requestDuration.labelValues(namespace, appName, method, uriPath, statusCode).observe(durationSeconds);
        }
    }
}
