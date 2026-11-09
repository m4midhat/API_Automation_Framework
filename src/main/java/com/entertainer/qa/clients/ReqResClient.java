package com.entertainer.qa.clients;

import com.entertainer.qa.config.ConfigManager;
import com.entertainer.qa.models.reqres.CreateUserRequest;
import com.entertainer.qa.models.reqres.LoginRequest;
import com.entertainer.qa.models.reqres.RegisterRequest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Thin wrapper over the ReqRes (https://reqres.in) endpoints used by this suite. Each method
 * returns the raw {@link Response} so test classes stay in control of their own assertions
 * instead of this client making assumptions about expected status codes.
 *
 * <p>Without an API key, ReqRes caps anonymous requests at 40/day per IP, which this suite's own
 * ~35 requests can consume in a single run. Set {@code reqres.api.key} (via
 * {@code -Dreqres.api.key=...} or the {@code REQRES_API_KEY} env var, after signing up for a free
 * key at https://app.reqres.in) to raise that to 250/day - see README.md.
 */
public class ReqResClient extends BaseApiClient {

    private static final String BASE_URL_CONFIG_KEY = "reqres.base.url";
    private static final String API_KEY_CONFIG_KEY = "reqres.api.key";

    private RequestSpecification spec() {
        String apiKey = ConfigManager.get(API_KEY_CONFIG_KEY);
        RequestSpecification spec = requestSpec(BASE_URL_CONFIG_KEY);
        return apiKey != null ? spec.header("x-api-key", apiKey) : spec;
    }

    public Response listUsers(int page) {
        return given().spec(spec())
                .queryParam("page", page)
                .when().get("/users");
    }

    public Response listUsers(int page, int delaySeconds) {
        return given().spec(spec())
                .queryParam("page", page)
                .queryParam("delay", delaySeconds)
                .when().get("/users");
    }

    public Response getUser(int id) {
        return given().spec(spec())
                .when().get("/users/{id}", id);
    }

    public Response createUser(CreateUserRequest request) {
        return given().spec(spec())
                .body(request)
                .when().post("/users");
    }

    public Response createUser(String rawJsonBody) {
        return given().spec(spec())
                .body(rawJsonBody)
                .when().post("/users");
    }

    public Response updateUserPut(int id, CreateUserRequest request) {
        return given().spec(spec())
                .body(request)
                .when().put("/users/{id}", id);
    }

    public Response updateUserPatch(int id, CreateUserRequest request) {
        return given().spec(spec())
                .body(request)
                .when().patch("/users/{id}", id);
    }

    public Response deleteUser(int id) {
        return given().spec(spec())
                .when().delete("/users/{id}", id);
    }

    public Response register(RegisterRequest request) {
        return given().spec(spec())
                .body(request)
                .when().post("/register");
    }

    public Response register(String rawJsonBody) {
        return given().spec(spec())
                .body(rawJsonBody)
                .when().post("/register");
    }

    public Response login(LoginRequest request) {
        return given().spec(spec())
                .body(request)
                .when().post("/login");
    }

    public Response login(String rawJsonBody) {
        return given().spec(spec())
                .body(rawJsonBody)
                .when().post("/login");
    }
}
