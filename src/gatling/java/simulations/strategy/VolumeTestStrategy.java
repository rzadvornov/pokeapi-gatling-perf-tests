package simulations.strategy;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import simulations.config.TestConfig;

import java.util.Arrays;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;

public class VolumeTestStrategy implements TestStrategy {

    @Override
    public List<PopulationBuilder> buildPopulation(
            ScenarioBuilder getPokemonById,
            ScenarioBuilder getPokemonByName,
            ScenarioBuilder listPokemon,
            ScenarioBuilder getPokemonAbilities,
            ScenarioBuilder mixedOperations,
            TestConfig config
    ) {
        var listPattern = config.getLoadPatterns().get("listPokemon");
        var byIdPattern = config.getLoadPatterns().get("getPokemonById");
        var abilitiesPattern = config.getLoadPatterns().get("getPokemonAbilities");

        return Arrays.asList(
                listPokemon.injectOpen(
                        rampUsersPerSec(listPattern.getRampFrom())
                                .to(listPattern.getRampTo())
                                .during(listPattern.getRampDuration()),
                        constantUsersPerSec(listPattern.getConstantRate())
                                .during(listPattern.getConstantDuration())
                ),
                getPokemonById.injectOpen(
                        rampUsersPerSec(byIdPattern.getRampFrom())
                                .to(byIdPattern.getRampTo())
                                .during(byIdPattern.getRampDuration()),
                        constantUsersPerSec(byIdPattern.getConstantRate())
                                .during(byIdPattern.getConstantDuration())
                ),
                getPokemonAbilities.injectOpen(
                        rampUsersPerSec(abilitiesPattern.getRampFrom())
                                .to(abilitiesPattern.getRampTo())
                                .during(abilitiesPattern.getRampDuration()),
                        constantUsersPerSec(abilitiesPattern.getConstantRate())
                                .during(abilitiesPattern.getConstantDuration())
                )
        );
    }

    @Override
    public void configureAssertions(Simulation.SetUp setUp, TestConfig config) {
        var assertion = config.getAssertions().get(TestType.VOLUME.getName());
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