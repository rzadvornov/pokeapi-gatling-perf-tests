package simulations.factory;

import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class ScenarioFactory {

    private static final String POKEMON_BASE_PATH = "/pokemon";

    public static ScenarioBuilder createGetPokemonById() {
        return scenario("Get Pokemon by ID")
                .exec(
                        http("Get Pikachu")
                                .get(POKEMON_BASE_PATH + "/25")
                                .check(status().is(200))
                                .check(jsonPath("$.name").is("pikachu"))
                )
                .pause(1);
    }

    public static ScenarioBuilder createGetPokemonByName() {
        return scenario("Get Pokemon by Name")
                .exec(
                        http("Get Charizard")
                                .get(POKEMON_BASE_PATH + "/charizard")
                                .check(status().is(200))
                                .check(jsonPath("$.id").is("6"))
                )
                .pause(1);
    }

    public static ScenarioBuilder createListPokemon() {
        return scenario("List Pokemon")
                .exec(
                        http("List Pokemon")
                                .get(POKEMON_BASE_PATH + "?limit=20&offset=0")
                                .check(status().is(200))
                                .check(jsonPath("$.results").exists())
                )
                .pause(1);
    }

    public static ScenarioBuilder createGetPokemonAbilities() {
        return scenario("Get Pokemon Abilities")
                .feed(csv("pokemon-ids.csv").random())
                .exec(
                        http("Get Pokemon and Ability")
                                .get(POKEMON_BASE_PATH + "/#{randomPokemon}")
                                .check(status().is(200))
                )
                .pause(1);
    }

    public static ScenarioBuilder createMixedOperations() {
        return scenario("Mixed Pokemon Operations")
                .feed(csv("pokemon-ids.csv").random())
                .exec(
                        http("Get Random Pokemon 1")
                                .get(POKEMON_BASE_PATH + "/#{randomPokemon}")
                                .check(status().is(200))
                )
                .pause(1, 2)
                .exec(
                        http("List Pokemon")
                                .get(POKEMON_BASE_PATH + "?limit=10")
                                .check(status().is(200))
                )
                .pause(1, 2)
                .feed(csv("pokemon-ids.csv").random())
                .exec(
                        http("Get Random Pokemon 2")
                                .get(POKEMON_BASE_PATH + "/#{randomPokemon}")
                                .check(status().is(200))
                );
    }
}