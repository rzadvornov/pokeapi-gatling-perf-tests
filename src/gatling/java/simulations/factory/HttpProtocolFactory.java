
package simulations.factory;

import io.gatling.javaapi.http.HttpProtocolBuilder;
import simulations.config.TestConfig;

import static io.gatling.javaapi.http.HttpDsl.http;

public class HttpProtocolFactory {

    public static HttpProtocolBuilder create(TestConfig config) {
        TestConfig.HttpConfig httpConfig = config.getHttp();

        return http
                .baseUrl(httpConfig.getBaseUrl())
                .acceptHeader(httpConfig.getAcceptHeader())
                .acceptEncodingHeader(httpConfig.getAcceptEncodingHeader())
                .userAgentHeader(httpConfig.getUserAgentHeader());
    }
}
