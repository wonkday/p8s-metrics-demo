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
    private static long internalCounter = 0;

    public static void main(String[] args) throws IOException, InterruptedException {

        //JvmMetrics.builder().register();

        // Note: uptime_seconds_total is not a great example:
        // The built-in JvmMetrics have an out-of-the-box metric named process_start_time_seconds
        // with the start timestamp in seconds, so if you want to know the uptime you can simply
        // run the Prometheus query
        //     time() - process_start_time_seconds
        // rather than creating a custom uptime metric.
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
                .labelNames("app", "method", "path", "status")
                .register();


        HTTPServer server = HTTPServer.builder().port(PORT_NUM).buildAndStart();
        System.out.println(
                "HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");


        while (true) {
            Thread.sleep(SLEEP_TIME_IN_SECONDS);
            uptimeCounter.inc();
            requestCounter.labelValues("demo-ms").inc();
            internalCounter++;

            long start = System.nanoTime();
            // Simulate request handling
            long randomSleepTimeForRequestSimulation = (long) (Math.random() * 500);
            //System.out.println("sleepTime:"+randomSleepTimeForRequestSimulation);
            Thread.sleep(randomSleepTimeForRequestSimulation);
            double durationSeconds = (System.nanoTime() - start) / 1e9;

            // Record the duration
            requestDuration.labelValues("demo-ms", "GET", "/domain/demo-api/{demoId}", "200").observe(durationSeconds);
            requestDuration.labelValues("demo-ms", "POST", "/domain/demo-api", "201").observe(durationSeconds);
            requestDuration.labelValues("demo-ms", "POST", "/domain/demo-api", "200").observe(durationSeconds);
            if(internalCounter % 7 == 0) {
                requestDuration.labelValues("demo-ms", "POST", "/domain/demo-api", "401").observe(durationSeconds);
            }
            if(internalCounter % 3 == 0) {
                requestDuration.labelValues("demo-ms", "GET", "/domain/demo-api/{demoId}", "404").observe(durationSeconds);
            }
            if(internalCounter % 5 == 0) {
                requestDuration.labelValues("demo-ms", "POST", "/domain/demo-api", "500").observe(durationSeconds);
            }
        }
    }
}
