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
                        rampUsersPerSec(pattern.phase1RampFrom())
                                .to(pattern.phase1RampTo())
                                .during(pattern.phase1RampDuration()),
                        constantUsersPerSec(pattern.phase1ConstantRate())
                                .during(pattern.phase1ConstantDuration()),
                        rampUsersPerSec(pattern.phase2RampFrom())
                                .to(pattern.phase2RampTo())
                                .during(pattern.phase2RampDuration()),
                        constantUsersPerSec(pattern.phase2ConstantRate())
                                .during(pattern.phase2ConstantDuration())
                )
        );
    }

    @Override
    public void configureAssertions(Simulation.SetUp setUp, TestConfig config) {
        var assertion = config.getAssertions().get(TestType.STRESS.getName());
        setUp.assertions(
                global().responseTime()
                        .max()
                        .lt(assertion.maxResponseTime())
        );
    }
}