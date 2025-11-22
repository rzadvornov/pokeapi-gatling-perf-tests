# Configuration Files Summary

## Overview
The configuration system uses a shared HTTP configuration file plus test-specific configuration files. This eliminates duplication and follows the DRY (Don't Repeat Yourself) principle.

## Configuration Architecture

### Shared Configuration
- **http-config.json** - Common HTTP settings used by all tests
  - Base URL
  - Accept headers
  - Encoding headers
  - User agent

### Test-Specific Configurations
Each test type has its own configuration file that defines:
- Load patterns (ramp-up, constant load, spikes, etc.)
- Test-specific assertions
- Scenario configurations

The `TestConfig.load()` method automatically:
1. Loads the test-specific config file
2. If no HTTP section exists, loads `http-config.json`
3. Merges them into a complete configuration

## Configuration Files

### 1. load-config.json (Load Test)
- **Test Type**: `load`
- **Purpose**: Gradual ramp-up to verify system under expected load
- **Scenarios**: getPokemonById, getPokemonByName, listPokemon, getPokemonAbilities
- **Pattern**: Ramp users then maintain constant load
- **Duration**: ~6 minutes
- **Key Metrics**:
  - Max Response Time: 5000ms
  - Success Rate: 95%

### 2. stress-config.json (Stress Test)
- **Test Type**: `stress`
- **Purpose**: Push system beyond normal capacity to find breaking point
- **Scenarios**: mixedOperations
- **Pattern**: Two-phase progressive increase
  - Phase 1: 1→50 users/sec (ramp 2min, hold 3min)
  - Phase 2: 50→100 users/sec (ramp 2min, hold 3min)
- **Duration**: ~10 minutes
- **Key Metrics**:
  - Max Response Time: 10000ms
- **Configurable Values**:
  - phase1RampFrom, phase1RampTo, phase1RampDuration
  - phase1ConstantRate, phase1ConstantDuration
  - phase2RampFrom, phase2RampTo, phase2RampDuration
  - phase2ConstantRate, phase2ConstantDuration

### 3. spike-config.json (Spike Test)
- **Test Type**: `spike`
- **Purpose**: Test sudden traffic surges
- **Scenarios**: getPokemonById, listPokemon
- **Pattern**: Three sudden spikes with pauses between
  - getPokemonById: pause 10s → 100 users → pause 30s → 200 users → pause 30s → 300 users
  - listPokemon: pause 10s → 50 users → pause 30s → 100 users → pause 30s → 150 users
- **Duration**: ~2 minutes
- **Key Metrics**:
  - Max Response Time: 15000ms
  - Success Rate: 80%
- **Configurable Values**:
  - pause1Duration, spike1Users
  - pause2Duration, spike2Users
  - pause3Duration, spike3Users

### 4. endurance-config.json (Endurance Test)
- **Test Type**: `endurance`
- **Purpose**: Verify stability over extended period
- **Scenarios**: getPokemonById, getPokemonByName, listPokemon
- **Pattern**: Sustained load for 1 hour
- **Duration**: ~62 minutes
- **Key Metrics**:
  - 95th Percentile: 5000ms
  - Success Rate: 95%

### 5. volume-config.json (Volume Test)
- **Test Type**: `volume`
- **Purpose**: Test large data handling
- **Scenarios**: listPokemon, getPokemonById, getPokemonAbilities
- **Pattern**: High request volume
- **Duration**: ~11 minutes
- **Key Metrics**:
  - Max Response Time: 8000ms
  - Success Rate: 90%

## Config File Mapping

```
TestType.LOAD      → test-config.json
TestType.STRESS    → stress-config.json
TestType.SPIKE     → spike-config.json
TestType.ENDURANCE → endurance-config.json
TestType.VOLUME    → volume-config.json
```

## Configuration Structure

### Shared HTTP Config (http-config.json)
```json
{
  "baseUrl": {"type": "string"},
  "acceptHeader": {"type": "string"},
  "acceptEncodingHeader": {"type": "string"},
  "userAgentHeader": {"type": "string"}
}
```

### Test-Specific Config (all test config files)
```json
{
  "loadPatterns": {
    "scenarioName": {
      "scenario": {"type": "string"},
      "rampFrom": {"type": "number"},
      "rampTo": {"type": "number"},
      "rampDuration": {"type": "number"},
      "constantRate": {"type": "number"},
      "constantDuration": {"type": "number"}
    }
  },
  "assertions": {
    "testType": {
      "maxResponseTime": {"type": "number"},
      "successRate": {"type": "number"},
      "percentile95": {"type": "number"}
    }
  }
}
```

### Stress Test Config (multi-phase pattern)
```json
{
  "loadPatterns": {
    "mixedOperations": {
      "scenario": {"type": "string"},
      "phase1RampFrom": {"type": "number"},
      "phase1RampTo": {"type": "number"},
      "phase1RampDuration": {"type": "number"},
      "phase1ConstantRate": {"type": "number"},
      "phase1ConstantDuration": {"type": "number"},
      "phase2RampFrom": {"type": "number"},
      "phase2RampTo": {"type": "number"},
      "phase2RampDuration": {"type": "number"},
      "phase2ConstantRate": {"type": "number"},
      "phase2ConstantDuration": {"type": "number"}
    }
  }
}
```

**Note:** Test configs no longer include HTTP settings - these are loaded from `http-config.json` automatically.

## Customization

### Change HTTP Settings (All Tests)
Edit `http-config.json` to change base URL, headers for all tests:
```bash
vim src/gatling/resources/http-config.json
```

### Override Config File
```bash
# Use custom config for any test type
./gradlew loadTest -DconfigFile=my-custom-config.json
```

### Modify Existing Test Config
Simply edit the JSON file for your test type:

```bash
# Edit load test configuration
vim src/gatling/resources/test-config.json

# Run with updated config
./gradlew loadTest
```

## Benefits of Separate HTTP Config

1. **DRY Principle**: HTTP settings defined once, used everywhere
2. **Easy Updates**: Change API endpoint or headers in one place
3. **Consistency**: All tests use the same HTTP configuration
4. **Flexibility**: Can still override HTTP settings in individual test configs if needed
5. **Maintainability**: Reduces config file size and complexity

## Best Practices

1. **HTTP settings**: Keep all HTTP configuration in `http-config.json` unless a test needs different settings
2. **Version control**: Commit all config changes with descriptive messages
3. **Environment-specific**: Create separate http-config files for dev/staging/prod if needed (e.g., `http-config-prod.json`)
4. **Documentation**: Document complex patterns or unusual values in separate documentation
5. **Validation**: Test config changes with small load first
6. **Override when needed**: Individual test configs can include their own `http` section to override defaults

## Adding New Config

1. Create new JSON file: `src/gatling/resources/my-test-config.json`
2. Add to TestType enum: `MY_TEST("mytest", new MyTestStrategy(), "my-test-config.json")`
3. Implement strategy: `MyTestStrategy.java`
4. Run: `./gradlew gatlingRun -DtestType=mytest`