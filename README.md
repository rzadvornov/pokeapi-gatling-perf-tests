# Gatling Performance Tests for PokeAPI

Comprehensive performance testing suite for https://pokeapi.co/api/v2/ using Gatling, Java, Gradle, and Docker.

## Test Types

### 1. **Load Test**
- **Purpose**: Verify system behavior under expected load
- **Pattern**: Gradual ramp-up to steady state
- **Users**: 1-18 concurrent users
- **Duration**: ~6 minutes
- **Assertions**: 95% success rate, max response time < 5s

### 2. **Stress Test**
- **Purpose**: Determine breaking point
- **Pattern**: Progressive increase beyond normal capacity
- **Users**: 1-100 concurrent users
- **Duration**: ~10 minutes
- **Assertions**: Max response time < 10s

### 3. **Spike Test**
- **Purpose**: Test sudden traffic surges
- **Pattern**: Abrupt user increases
- **Users**: Multiple spikes up to 300 users
- **Duration**: ~2 minutes
- **Assertions**: 80% success rate, max response time < 15s

### 4. **Endurance Test**
- **Purpose**: Verify stability over extended period
- **Pattern**: Sustained load
- **Users**: 30 concurrent users
- **Duration**: ~62 minutes
- **Assertions**: 95% success rate, 95th percentile < 5s

### 5. **Volume Test**
- **Purpose**: Test large data handling
- **Pattern**: High request volume
- **Users**: 50 concurrent users
- **Duration**: ~11 minutes
- **Assertions**: 90% success rate, max response time < 8s

## Project Structure

```
.
├── src/
│   └── gatling/
│       ├── java/
│       │   └── simulations/
│       │       ├── PokemonSimulation.java (Main)
│       │       ├── config/
│       │       │   └── TestConfig.java
│       │       ├── factory/
│       │       │   ├── HttpProtocolFactory.java
│       │       │   └── ScenarioFactory.java
│       │       └── strategy/
│       │           ├── TestStrategy.java (Interface)
│       │           ├── TestType.java (Enum)
│       │           ├── LoadTestStrategy.java
│       │           ├── StressTestStrategy.java
│       │           ├── SpikeTestStrategy.java
│       │           ├── EnduranceTestStrategy.java
│       │           └── VolumeTestStrategy.java
│       └── resources/
│           ├── test-config.json
│           ├── endurance-config.json
│           ├── volume-config.json
│           └── pokemon-ids.csv
├── .github/
│   └── workflows/
│       └── performance-tests.yml
├── build.gradle
├── settings.gradle
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## Architecture

This project follows SOLID principles:

- **Single Responsibility**: Each class has one clear purpose
    - `TestConfig`: Configuration management
    - `ScenarioFactory`: Scenario creation
    - `HttpProtocolFactory`: HTTP protocol configuration
    - Strategy classes: Test execution logic

- **Open/Closed**: Easy to add new test types without modifying existing code
    - Add new strategy class
    - Add new enum value
    - Add new config file

- **Liskov Substitution**: All strategies implement `TestStrategy` interface

- **Interface Segregation**: Clean, focused interfaces

- **Dependency Inversion**: Depend on abstractions (TestStrategy) not concrete implementations

## Prerequisites

- Java 11 or higher
- Gradle 8.x
- Docker and Docker Compose
- Git

## Local Setup

### 1. Clone the repository
```bash
git clone <your-repo-url>
cd gatling-performance-tests
```

### 2. Create CSV feeder file
Create `src/gatling/resources/pokemon-ids.csv`:
```csv
randomPokemon
1
4
7
25
39
52
63
74
81
94
100
133
143
150
```

### 3. Build the project
```bash
./gradlew build
```

## Running Tests

### Using Gradle (Local)

Run individual tests:
```bash
# Load test
./gradlew loadTest

# Stress test
./gradlew stressTest

# Spike test
./gradlew spikeTest

# Endurance test
./gradlew enduranceTest

# Volume test
./gradlew volumeTest
```

### Using Docker

Build the image:
```bash
docker build -t gatling-performance-test .
```

Run a specific test:
```bash
# Load test
docker run --rm -v $(pwd)/results:/app/build/reports/gatling \
  -e TEST_TYPE=load gatling-performance-test

# Stress test
docker run --rm -v $(pwd)/results:/app/build/reports/gatling \
  -e TEST_TYPE=stress gatling-performance-test
```

### Using Docker Compose

Run individual tests:
```bash
# Load test
docker-compose up gatling-load-test

# Stress test
docker-compose up gatling-stress-test

# All tests (one at a time)
docker-compose up
```

## GitHub Actions

The workflow is configured to run:
- On push to `main` or `develop` branches
- On pull requests to `main`
- Daily at 2 AM UTC (scheduled)
- Manually via workflow dispatch

### Manual Trigger

1. Go to **Actions** tab in GitHub
2. Select **Performance Tests** workflow
3. Click **Run workflow**
4. Choose test type:
    - `load` - Run only load test
    - `stress` - Run only stress test
    - `spike` - Run only spike test
    - `endurance` - Run only endurance test
    - `volume` - Run only volume test
    - `all` - Run all tests

### Viewing Results

After test completion:
1. Go to the workflow run
2. Download artifacts (e.g., `load-test-results`)
3. Open `index.html` in a browser to view detailed Gatling reports

## Test Results

Results are saved in:
- **Local**: `build/reports/gatling/`
- **Docker**: Mounted volume location
- **GitHub**: Artifacts (available for 30 days)

Each test generates:
- HTML report with charts
- Request/response statistics
- Error analysis
- Performance metrics

## Customization

### Modify Test Parameters

Edit the JSON config files in `src/gatling/resources/`:
- `load-config.json` - Default configuration for load test
- `endurance-config.json` - Endurance test specific config
- `volume-config.json` - Volume test specific config
- `spike-config.json` - Spike test specific config
- `stress-config.json` - Stress test specific config
- `http-config.json` - Common http test config

Example configuration:
```json
{
  "loadPatterns": {
    "getPokemonById": {
      "rampFrom": 1,
      "rampTo": 10,
      "rampDuration": 60,
      "constantRate": 10,
      "constantDuration": 300
    }
  },
  "assertions": {
    "load": {
      "maxResponseTime": 5000,
      "successRate": 95.0
    }
  }
}
```

### Add New Test Type

1. Create a new strategy class implementing `TestStrategy`:
```java
public class CustomTestStrategy implements TestStrategy {
    @Override
    public List<PopulationBuilder> buildPopulation(...) {
        // Your logic here
    }
    
    @Override
    public void configureAssertions(...) {
        // Your assertions here
    }
}
```

2. Add to `TestType` enum:
```java
CUSTOM("custom", new CustomTestStrategy())
```

3. Create config file: `custom-config.json`

### Add New Scenarios

Add to `ScenarioFactory.java`:
```java
public static ScenarioBuilder createNewScenario() {
    return scenario("New Scenario")
        .exec(
            http("Request Name")
                .get("/endpoint")
                .check(status().is(200))
        );
}
```

## API Endpoints Tested

- `GET /pokemon/{id}` - Get Pokemon by ID
- `GET /pokemon/{name}` - Get Pokemon by name
- `GET /pokemon?limit={n}&offset={n}` - List Pokemon
- `GET /ability/{id}` - Get Pokemon abilities

## Performance Metrics

Gatling tracks:
- Response time (min, max, mean, percentiles)
- Requests per second
- Success/failure rates
- Active users over time
- Response time distribution

## Troubleshooting

### Docker build fails
```bash
# Clean and rebuild
docker system prune -a
docker build --no-cache -t gatling-performance-test .
```

### Gradle build fails
```bash
# Clean build
./gradlew clean build --refresh-dependencies
```

### Tests timing out
- Check internet connection
- Verify PokeAPI is accessible
- Increase timeout in assertions
- Reduce concurrent users

## Best Practices

1. **Start Small**: Begin with load tests before stress/spike tests
2. **Monitor API**: Respect PokeAPI rate limits
3. **Analyze Results**: Review reports after each test
4. **Iterate**: Adjust parameters based on findings
5. **Document**: Keep notes on test outcomes

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

MIT License - feel free to use and modify for your needs.

## Resources

- [Gatling Documentation](https://gatling.io/docs/current/)
- [PokeAPI Documentation](https://pokeapi.co/docs/v2)
- [Gradle Documentation](https://docs.gradle.org/)

## Support

For issues or questions:
- Open an issue in the repository
- Check Gatling community forums
- Review PokeAPI documentation