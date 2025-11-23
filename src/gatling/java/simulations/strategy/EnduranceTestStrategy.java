package simulations.strategy;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import simulations.config.TestConfig;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;
import static java.util.Arrays.asList;

public class EnduranceTestStrategy implements TestStrategy {

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
        var byNamePattern = config.getLoadPatterns().get("getPokemonByName");
        var listPattern = config.getLoadPatterns().get("listPokemon");

        return asList(
                getPokemonById.injectOpen(
                        rampUsersPerSec(byIdPattern.rampFrom())
                                .to(byIdPattern.rampTo())
                                .during(byIdPattern.rampDuration()),
                        constantUsersPerSec(byIdPattern.constantRate())
                                .during(byIdPattern.constantDuration())
                ),
                getPokemonByName.injectOpen(
                        rampUsersPerSec(byNamePattern.rampFrom())
                                .to(byNamePattern.rampTo())
                                .during(byNamePattern.rampDuration()),
                        constantUsersPerSec(byNamePattern.constantRate())
                                .during(byNamePattern.constantDuration())
                ),
                listPokemon.injectOpen(
                        rampUsersPerSec(listPattern.rampFrom())
                                .to(listPattern.rampTo())
                                .during(listPattern.rampDuration()),
                        constantUsersPerSec(listPattern.constantRate())
                                .during(listPattern.constantDuration())
                )
        );
    }

    @Override
    public void configureAssertions(Simulation.SetUp setUp, TestConfig config) {
        var assertion = config.getAssertions().get(TestType.ENDURANCE.getName());
        setUp.assertions(
                global().responseTime()
                        .percentile(assertion.successRate())
                        .lt(assertion.percentile95()),
                global().successfulRequests()
                        .percent()
                        .gt(assertion.successRate())
        );
    }
}