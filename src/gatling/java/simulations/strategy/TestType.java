package simulations.strategy;

public enum TestType {
    LOAD("load", new LoadTestStrategy(), "load-config.json"),
    STRESS("stress", new StressTestStrategy(), "stress-config.json"),
    SPIKE("spike", new SpikeTestStrategy(), "spike-config.json"),
    ENDURANCE("endurance", new EnduranceTestStrategy(), "endurance-config.json"),
    VOLUME("volume", new VolumeTestStrategy(), "volume-config.json");

    private final String name;
    private final TestStrategy strategy;
    private final String configFile;

    TestType(String name, TestStrategy strategy, String configFile) {
        this.name = name;
        this.strategy = strategy;
        this.configFile = configFile;
    }

    public String getName() {
        return name;
    }

    public TestStrategy getStrategy() {
        return strategy;
    }

    public String getConfigFile() {
        return configFile;
    }

    public static TestType fromString(String name) {
        for (TestType type : TestType.values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return LOAD;
    }
}