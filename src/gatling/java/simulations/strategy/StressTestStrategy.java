package simulations.strategy;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import simulations.config.TestConfig;

import java.util.Collections;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;

public class StressTestStrategy implements TestStrategy {

    @Override
    public List<PopulationBuilder> buildPopulation(
            ScenarioBuilder getPokemonById,
            ScenarioBuilder getPokemonByName,
            ScenarioBuilder listPokemon,
            ScenarioBuilder getPokemonAbilities,
            ScenarioBuilder mixedOperations,
            TestConfig config
    ) {
        var pattern = config.getLoadPatterns().get("mixedOperations");

        return Collections.singletonList(
                mixedOperations.injectOpen(
                        rampUsersPerSec(pattern.getPhase1RampFrom())
                                .to(pattern.getPhase1RampTo())
                                .during(pattern.getPhase1RampDuration()),
                        constantUsersPerSec(pattern.getPhase1ConstantRate())
                                .during(pattern.getPhase1ConstantDuration()),
                        rampUsersPerSec(pattern.getPhase2RampFrom())
                                .to(pattern.getPhase2RampTo())
                                .during(pattern.getPhase2RampDuration()),
                        constantUsersPerSec(pattern.getPhase2ConstantRate())
                                .during(pattern.getPhase2ConstantDuration())
                )
        );
    }

    @Override
    public void configureAssertions(Simulation.SetUp setUp, TestConfig config) {
        var assertion = config.getAssertions().get(TestType.STRESS.getName());
        setUp.assertions(
                global().responseTime()
                        .max()
                        .lt(assertion.getMaxResponseTime())
        );
    }
}