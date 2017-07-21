package com.github.gavlyukovskiy.sample;

import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.IllegalSelectorException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@Fork(value = 2)
public class TracingBenchmark {

    private String basePort;
    private String sleuthPort;
    private String sleuthDecoratorPort;

    @Setup
    public void setup() throws IOException {
        this.basePort = Files.lines(Paths.get("base.port")).findFirst().orElseThrow(IllegalSelectorException::new);
        this.sleuthPort = Files.lines(Paths.get("sleuth.port")).findFirst().orElseThrow(IllegalSelectorException::new);
        this.sleuthDecoratorPort = Files.lines(Paths.get("sleuth_decorator.port")).findFirst().orElseThrow(IllegalSelectorException::new);
    }

    @Benchmark
    public void base() throws IOException {
        rest(basePort);
    }

    @Benchmark
    public void sleuth() throws IOException {
        rest(sleuthPort);
    }

    @Benchmark
    public void sleuth_decorator() throws IOException {
        rest(sleuthDecoratorPort);
    }

    private void rest(String port) throws IOException {
        URL url = new URL("http://localhost:" + port + "/select/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        IOUtils.toString(conn.getInputStream());
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(TracingBenchmark.class.getSimpleName())
                .build();
//        Process base = Runtime.getRuntime()
//                .exec("java -jar datasource-decorator-benchmark/build/sample-service.jar base --spring.sleuth.enabled=false --spring.datasource.decorator.enabled=false");
//
//        Process sleuth = Runtime.getRuntime()
//                .exec("java -jar datasource-decorator-benchmark/build/sample-service.jar sleuth --spring.sleuth.enabled=true --spring.datasource.decorator.enabled=false");
//
//        Process sleuth_decorator = Runtime.getRuntime()
//                .exec("java -jar datasource-decorator-benchmark/build/sample-service.jar sleuth_decorator --spring.sleuth.enabled=true --spring.datasource.decorator.enabled=true");
//
//        waitForApplicationStart();

        new Runner(opt).run();

//        base.destroy();
//        sleuth.destroy();
//        sleuth_decorator.destroy();
    }

    private static void waitForApplicationStart() throws InterruptedException {
        while (!Files.exists(Paths.get("base.port"))
                || !Files.exists(Paths.get("sleuth.port"))
                || !Files.exists(Paths.get("sleuth_decorator.port"))) {
            System.out.println("Waiting to application start");
            Thread.sleep(1000L);
        }
    }
}
