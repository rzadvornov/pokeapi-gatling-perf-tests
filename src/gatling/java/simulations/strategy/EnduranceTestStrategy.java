package simulations.strategy;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import simulations.config.TestConfig;

import java.util.Arrays;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;

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

        return Arrays.asList(
                getPokemonById.injectOpen(
                        rampUsersPerSec(byIdPattern.getRampFrom())
                                .to(byIdPattern.getRampTo())
                                .during(byIdPattern.getRampDuration()),
                        constantUsersPerSec(byIdPattern.getConstantRate())
                                .during(byIdPattern.getConstantDuration())
                ),
                getPokemonByName.injectOpen(
                        rampUsersPerSec(byNamePattern.getRampFrom())
                                .to(byNamePattern.getRampTo())
                                .during(byNamePattern.getRampDuration()),
                        constantUsersPerSec(byNamePattern.getConstantRate())
                                .during(byNamePattern.getConstantDuration())
                ),
                listPokemon.injectOpen(
                        rampUsersPerSec(listPattern.getRampFrom())
                                .to(listPattern.getRampTo())
                                .during(listPattern.getRampDuration()),
                        constantUsersPerSec(listPattern.getConstantRate())
                                .during(listPattern.getConstantDuration())
                )
        );
    }

    @Override
    public void configureAssertions(Simulation.SetUp setUp, TestConfig config) {
        var assertion = config.getAssertions().get(TestType.ENDURANCE.getName());
        setUp.assertions(
                global().responseTime()
                        .percentile(assertion.getSuccessRate())
                        .lt(assertion.getPercentile95()),
                global().successfulRequests()
                        .percent()
                        .gt(assertion.getSuccessRate())
        );
    }
}