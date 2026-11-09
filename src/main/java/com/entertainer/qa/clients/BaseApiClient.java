package com.entertainer.qa.clients;

import com.entertainer.qa.config.ConfigManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.config.HttpClientConfig.httpClientConfig;

/**
 * Builds the shared {@link RequestSpecification} every API client starts from: base URL, JSON
 * content type, connection/read timeouts sourced from {@link ConfigManager}, and the logging /
 * Allure filters used for reporting.
 */
public abstract class BaseApiClient {

    protected final RequestSpecification requestSpec(String baseUrlConfigKey) {
        int connectionTimeoutMs = ConfigManager.getInt("http.connection.timeout.ms", 10_000);
        int readTimeoutMs = ConfigManager.getInt("http.read.timeout.ms", 15_000);

        RestAssuredConfig config = RestAssuredConfig.config().httpClient(
                httpClientConfig()
                        // Apache HttpClient parameter names - see org.apache.http.params.CoreConnectionPNames
                        .setParam("http.connection.timeout", connectionTimeoutMs)
                        .setParam("http.socket.timeout", readTimeoutMs));

        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.get(baseUrlConfigKey))
                .setContentType(ContentType.JSON)
                // A literal single value, not ContentType.JSON's broad "application/json,
                // application/javascript, text/javascript, text/json" accept-list: Restful-Booker
                // rejects that multi-value Accept header with 418 (strict content negotiation).
                .addHeader("Accept", "application/json")
                .setConfig(config)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new AllureRestAssured())
                .build();
    }
}
