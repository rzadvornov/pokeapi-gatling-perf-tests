package simulations;

import io.gatling.javaapi.core.Simulation;
import simulations.config.TestConfig;
import simulations.factory.HttpProtocolFactory;
import simulations.factory.ScenarioFactory;
import simulations.strategy.TestType;

import java.io.IOException;

public class PokemonSimulation extends Simulation {

    private static final String DEFAULT_TEST_TYPE = "load";

    {
        try {
            // Determine test type
            String testTypeName = System.getProperty("testType", DEFAULT_TEST_TYPE);
            TestType testType = TestType.fromString(testTypeName);

            // Load configuration from the test type's config file (or override)
            String configFile = System.getProperty("configFile", testType.getConfigFile());
            TestConfig config = TestConfig.load(configFile);

            // Create HTTP protocol
            var httpProtocol = HttpProtocolFactory.create(config);

            // Create scenarios
            var getPokemonById = ScenarioFactory.createGetPokemonById();
            var getPokemonByName = ScenarioFactory.createGetPokemonByName();
            var listPokemon = ScenarioFactory.createListPokemon();
            var getPokemonAbilities = ScenarioFactory.createGetPokemonAbilities();
            var mixedOperations = ScenarioFactory.createMixedOperations();

            // Build population using strategy
            var population = testType.getStrategy().buildPopulation(
                    getPokemonById,
                    getPokemonByName,
                    listPokemon,
                    getPokemonAbilities,
                    mixedOperations,
                    config
            );

            // Setup simulation
            var setUp = setUp(population).protocols(httpProtocol);

            // Configure assertions using strategy
            testType.getStrategy().configureAssertions(setUp, config);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load test configuration", e);
        }
    }
}