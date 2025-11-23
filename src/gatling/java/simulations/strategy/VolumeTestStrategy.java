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
                        rampUsersPerSec(listPattern.rampFrom())
                                .to(listPattern.rampTo())
                                .during(listPattern.rampDuration()),
                        constantUsersPerSec(listPattern.constantRate())
                                .during(listPattern.constantDuration())
                ),
                getPokemonById.injectOpen(
                        rampUsersPerSec(byIdPattern.rampFrom())
                                .to(byIdPattern.rampTo())
                                .during(byIdPattern.rampDuration()),
                        constantUsersPerSec(byIdPattern.constantRate())
                                .during(byIdPattern.constantDuration())
                ),
                getPokemonAbilities.injectOpen(
                        rampUsersPerSec(abilitiesPattern.rampFrom())
                                .to(abilitiesPattern.rampTo())
                                .during(abilitiesPattern.rampDuration()),
                        constantUsersPerSec(abilitiesPattern.constantRate())
                                .during(abilitiesPattern.constantDuration())
                )
        );
    }

    @Override
    public void configureAssertions(Simulation.SetUp setUp, TestConfig config) {
        var assertion = config.getAssertions().get(TestType.VOLUME.getName());
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