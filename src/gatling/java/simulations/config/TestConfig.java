package simulations.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class TestConfig {
    private HttpConfig http;
    private Map<String, LoadPattern> loadPatterns;
    private Map<String, Assertion> assertions;

    private static final String DEFAULT_HTTP_CONFIG = "http-config.json";

    public static TestConfig load(String configFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        InputStream is = TestConfig.class.getClassLoader().getResourceAsStream(configFile);
        if (is == null) {
            throw new IOException("Config file not found: " + configFile);
        }
        TestConfig config = mapper.readValue(is, TestConfig.class);

        if (config.getHttp() == null) { // Retaining getter/setter logic for Jackson compatibility in outer class
            InputStream httpIs = TestConfig.class.getClassLoader().getResourceAsStream(DEFAULT_HTTP_CONFIG);
            if (httpIs != null) {
                HttpConfig httpConfig = mapper.readValue(httpIs, HttpConfig.class);
                config.setHttp(httpConfig);
            } else {
                throw new IOException("HTTP config file not found: " + DEFAULT_HTTP_CONFIG);
            }
        }

        return config;
    }

    public HttpConfig getHttp() {
        return http;
    }

    public void setHttp(HttpConfig http) {
        this.http = http;
    }

    public Map<String, LoadPattern> getLoadPatterns() {
        return loadPatterns;
    }

    public void setLoadPatterns(Map<String, LoadPattern> loadPatterns) {
        this.loadPatterns = loadPatterns;
    }

    public Map<String, Assertion> getAssertions() {
        return assertions;
    }

    public void setAssertions(Map<String, Assertion> assertions) {
        this.assertions = assertions;
    }

    public record HttpConfig(
            String baseUrl,
            String acceptHeader,
            String acceptEncodingHeader,
            String userAgentHeader
    ) {}

    public record LoadPattern(
            String scenario,
            Integer rampFrom,
            Integer rampTo,
            Integer rampDuration,
            Integer constantRate,
            Integer constantDuration,
            Integer atOnceUsers,
            Integer nothingForDuration,
            // Multi-phase pattern components
            Integer phase1RampFrom,
            Integer phase1RampTo,
            Integer phase1RampDuration,
            Integer phase1ConstantRate,
            Integer phase1ConstantDuration,
            Integer phase2RampFrom,
            Integer phase2RampTo,
            Integer phase2RampDuration,
            Integer phase2ConstantRate,
            Integer phase2ConstantDuration,
            // Spike pattern components
            Integer pause1Duration,
            Integer spike1Users,
            Integer pause2Duration,
            Integer spike2Users,
            Integer pause3Duration,
            Integer spike3Users
    ) {}

    public record Assertion(
            Integer maxResponseTime,
            Double successRate,
            Integer percentile95
    ) {}
}