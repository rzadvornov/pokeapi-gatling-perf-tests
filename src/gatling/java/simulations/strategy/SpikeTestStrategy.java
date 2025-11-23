package simulations.strategy;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import simulations.config.TestConfig;

import java.util.Arrays;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;

public class SpikeTestStrategy implements TestStrategy {

    @Override
    public List<PopulationBuilder> buildPopulation(
            ScenarioBuilder getPokemonById,
            ScenarioBuilder getPokemonByName,
            ScenarioBuilder listPokemon,
            ScenarioBuilder getPokemonAbilities,
            ScenarioBuilder mixedOperations,
            TestConfig config
    ) {
        var byIdPattern = config.getLoadPatterns().get("getPokemonById");
        var listPattern = config.getLoadPatterns().get("listPokemon");

        return Arrays.asList(
                getPokemonById.injectOpen(
                        nothingFor(byIdPattern.pause1Duration()),
                        atOnceUsers(byIdPattern.spike1Users()),
                        nothingFor(byIdPattern.pause2Duration()),
                        atOnceUsers(byIdPattern.spike2Users()),
                        nothingFor(byIdPattern.pause3Duration()),
                        atOnceUsers(byIdPattern.spike3Users())
                ),
                listPokemon.injectOpen(
                        nothingFor(listPattern.pause1Duration()),
                        atOnceUsers(listPattern.spike1Users()),
                        nothingFor(listPattern.pause2Duration()),
                        atOnceUsers(listPattern.spike2Users()),
                        nothingFor(listPattern.pause3Duration()),
                        atOnceUsers(listPattern.spike3Users())
                )
        );
    }

    @Override
    public void configureAssertions(Simulation.SetUp setUp, TestConfig config) {
        var assertion = config.getAssertions().get(TestType.SPIKE.getName());
        setUp.assertions(
                global().responseTime()
                        .max()
                        .lt(assertion.maxResponseTime()),
                global().successfulRequests()
                        .percent()
                        .gt(assertion.successRate())
        );
    }
}