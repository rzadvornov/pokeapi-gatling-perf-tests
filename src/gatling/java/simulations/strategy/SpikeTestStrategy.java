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
                        nothingFor(byIdPattern.getPause1Duration()),
                        atOnceUsers(byIdPattern.getSpike1Users()),
                        nothingFor(byIdPattern.getPause2Duration()),
                        atOnceUsers(byIdPattern.getSpike2Users()),
                        nothingFor(byIdPattern.getPause3Duration()),
                        atOnceUsers(byIdPattern.getSpike3Users())
                ),
                listPokemon.injectOpen(
                        nothingFor(listPattern.getPause1Duration()),
                        atOnceUsers(listPattern.getSpike1Users()),
                        nothingFor(listPattern.getPause2Duration()),
                        atOnceUsers(listPattern.getSpike2Users()),
                        nothingFor(listPattern.getPause3Duration()),
                        atOnceUsers(listPattern.getSpike3Users())
                )
        );
    }

    @Override
    public void configureAssertions(Simulation.SetUp setUp, TestConfig config) {
        var assertion = config.getAssertions().get(TestType.SPIKE.getName());
        setUp.assertions(
                global().responseTime()
                        .max()
                        .lt(assertion.getMaxResponseTime()),
                global().successfulRequests()
                        .percent()
                        .gt(assertion.getSuccessRate())
        );
    }
}