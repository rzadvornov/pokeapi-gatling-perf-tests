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

        if (config.getHttp() == null) {
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

    public static class HttpConfig {
        private String baseUrl;
        private String acceptHeader;
        private String acceptEncodingHeader;
        private String userAgentHeader;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getAcceptHeader() {
            return acceptHeader;
        }

        public void setAcceptHeader(String acceptHeader) {
            this.acceptHeader = acceptHeader;
        }

        public String getAcceptEncodingHeader() {
            return acceptEncodingHeader;
        }

        public void setAcceptEncodingHeader(String acceptEncodingHeader) {
            this.acceptEncodingHeader = acceptEncodingHeader;
        }

        public String getUserAgentHeader() {
            return userAgentHeader;
        }

        public void setUserAgentHeader(String userAgentHeader) {
            this.userAgentHeader = userAgentHeader;
        }
    }

    public static class LoadPattern {
        private String scenario;
        private Integer rampFrom;
        private Integer rampTo;
        private Integer rampDuration;
        private Integer constantRate;
        private Integer constantDuration;
        private Integer atOnceUsers;
        private Integer nothingForDuration;

        // Multi-phase pattern fields (for stress test)
        private Integer phase1RampFrom;
        private Integer phase1RampTo;
        private Integer phase1RampDuration;
        private Integer phase1ConstantRate;
        private Integer phase1ConstantDuration;
        private Integer phase2RampFrom;
        private Integer phase2RampTo;
        private Integer phase2RampDuration;
        private Integer phase2ConstantRate;
        private Integer phase2ConstantDuration;

        // Spike pattern fields (for spike test)
        private Integer pause1Duration;
        private Integer spike1Users;
        private Integer pause2Duration;
        private Integer spike2Users;
        private Integer pause3Duration;
        private Integer spike3Users;

        public String getScenario() {
            return scenario;
        }

        public void setScenario(String scenario) {
            this.scenario = scenario;
        }

        public Integer getRampFrom() {
            return rampFrom;
        }

        public void setRampFrom(Integer rampFrom) {
            this.rampFrom = rampFrom;
        }

        public Integer getRampTo() {
            return rampTo;
        }

        public void setRampTo(Integer rampTo) {
            this.rampTo = rampTo;
        }

        public Integer getRampDuration() {
            return rampDuration;
        }

        public void setRampDuration(Integer rampDuration) {
            this.rampDuration = rampDuration;
        }

        public Integer getConstantRate() {
            return constantRate;
        }

        public void setConstantRate(Integer constantRate) {
            this.constantRate = constantRate;
        }

        public Integer getConstantDuration() {
            return constantDuration;
        }

        public void setConstantDuration(Integer constantDuration) {
            this.constantDuration = constantDuration;
        }

        public Integer getAtOnceUsers() {
            return atOnceUsers;
        }

        public void setAtOnceUsers(Integer atOnceUsers) {
            this.atOnceUsers = atOnceUsers;
        }

        public Integer getNothingForDuration() {
            return nothingForDuration;
        }

        public void setNothingForDuration(Integer nothingForDuration) {
            this.nothingForDuration = nothingForDuration;
        }

        public Integer getPhase1RampFrom() {
            return phase1RampFrom;
        }

        public void setPhase1RampFrom(Integer phase1RampFrom) {
            this.phase1RampFrom = phase1RampFrom;
        }

        public Integer getPhase1RampTo() {
            return phase1RampTo;
        }

        public void setPhase1RampTo(Integer phase1RampTo) {
            this.phase1RampTo = phase1RampTo;
        }

        public Integer getPhase1RampDuration() {
            return phase1RampDuration;
        }

        public void setPhase1RampDuration(Integer phase1RampDuration) {
            this.phase1RampDuration = phase1RampDuration;
        }

        public Integer getPhase1ConstantRate() {
            return phase1ConstantRate;
        }

        public void setPhase1ConstantRate(Integer phase1ConstantRate) {
            this.phase1ConstantRate = phase1ConstantRate;
        }

        public Integer getPhase1ConstantDuration() {
            return phase1ConstantDuration;
        }

        public void setPhase1ConstantDuration(Integer phase1ConstantDuration) {
            this.phase1ConstantDuration = phase1ConstantDuration;
        }

        public Integer getPhase2RampFrom() {
            return phase2RampFrom;
        }

        public void setPhase2RampFrom(Integer phase2RampFrom) {
            this.phase2RampFrom = phase2RampFrom;
        }

        public Integer getPhase2RampTo() {
            return phase2RampTo;
        }

        public void setPhase2RampTo(Integer phase2RampTo) {
            this.phase2RampTo = phase2RampTo;
        }

        public Integer getPhase2RampDuration() {
            return phase2RampDuration;
        }

        public void setPhase2RampDuration(Integer phase2RampDuration) {
            this.phase2RampDuration = phase2RampDuration;
        }

        public Integer getPhase2ConstantRate() {
            return phase2ConstantRate;
        }

        public void setPhase2ConstantRate(Integer phase2ConstantRate) {
            this.phase2ConstantRate = phase2ConstantRate;
        }

        public Integer getPhase2ConstantDuration() {
            return phase2ConstantDuration;
        }

        public void setPhase2ConstantDuration(Integer phase2ConstantDuration) {
            this.phase2ConstantDuration = phase2ConstantDuration;
        }

        public Integer getPause1Duration() {
            return pause1Duration;
        }

        public void setPause1Duration(Integer pause1Duration) {
            this.pause1Duration = pause1Duration;
        }

        public Integer getSpike1Users() {
            return spike1Users;
        }

        public void setSpike1Users(Integer spike1Users) {
            this.spike1Users = spike1Users;
        }

        public Integer getPause2Duration() {
            return pause2Duration;
        }

        public void setPause2Duration(Integer pause2Duration) {
            this.pause2Duration = pause2Duration;
        }

        public Integer getSpike2Users() {
            return spike2Users;
        }

        public void setSpike2Users(Integer spike2Users) {
            this.spike2Users = spike2Users;
        }

        public Integer getPause3Duration() {
            return pause3Duration;
        }

        public void setPause3Duration(Integer pause3Duration) {
            this.pause3Duration = pause3Duration;
        }

        public Integer getSpike3Users() {
            return spike3Users;
        }

        public void setSpike3Users(Integer spike3Users) {
            this.spike3Users = spike3Users;
        }
    }

    public static class Assertion {
        private Integer maxResponseTime;
        private Double successRate;
        private Integer percentile95;

        public Integer getMaxResponseTime() {
            return maxResponseTime;
        }

        public void setMaxResponseTime(Integer maxResponseTime) {
            this.maxResponseTime = maxResponseTime;
        }

        public Double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(Double successRate) {
            this.successRate = successRate;
        }

        public Integer getPercentile95() {
            return percentile95;
        }

        public void setPercentile95(Integer percentile95) {
            this.percentile95 = percentile95;
        }
    }
}