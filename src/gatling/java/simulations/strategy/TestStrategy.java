
package simulations.strategy;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import simulations.config.TestConfig;

import java.util.List;

public interface TestStrategy {
    List<PopulationBuilder> buildPopulation(
            ScenarioBuilder getPokemonById,
            ScenarioBuilder getPokemonByName,
            ScenarioBuilder listPokemon,
            ScenarioBuilder getPokemonAbilities,
            ScenarioBuilder mixedOperations,
            TestConfig config
    );

    void configureAssertions(io.gatling.javaapi.core.Simulation.SetUp setUp, TestConfig config);
}