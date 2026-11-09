package com.entertainer.qa.clients;

import com.entertainer.qa.models.booker.AuthRequest;
import com.entertainer.qa.models.booker.Booking;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Thin wrapper over the Restful-Booker (https://restful-booker.herokuapp.com) endpoints used by
 * this suite. Write operations (PUT/PATCH/DELETE) are authenticated via the {@code token} cookie
 * returned by {@link #auth(AuthRequest)}, the primary auth mechanism documented by the API.
 */
public class BookerClient extends BaseApiClient {

    private static final String BASE_URL_CONFIG_KEY = "booker.base.url";

    private RequestSpecification spec() {
        return requestSpec(BASE_URL_CONFIG_KEY);
    }

    public Response ping() {
        return given().spec(spec())
                .when().get("/ping");
    }

    public Response auth(AuthRequest request) {
        return given().spec(spec())
                .body(request)
                .when().post("/auth");
    }

    public Response auth(String rawJsonBody) {
        return given().spec(spec())
                .body(rawJsonBody)
                .when().post("/auth");
    }

    public Response createBooking(Booking booking) {
        return given().spec(spec())
                .body(booking)
                .when().post("/booking");
    }

    public Response getBookingIds() {
        return given().spec(spec())
                .when().get("/booking");
    }

    public Response getBookingIds(Map<String, ?> queryParams) {
        return given().spec(spec())
                .queryParams(queryParams)
                .when().get("/booking");
    }

    public Response getBooking(int bookingId) {
        return given().spec(spec())
                .when().get("/booking/{id}", bookingId);
    }

    public Response updateBookingPut(int bookingId, Booking booking, String token) {
        RequestSpecification request = given().spec(spec()).body(booking);
        if (token != null) {
            request.cookie("token", token);
        }
        return request.when().put("/booking/{id}", bookingId);
    }

    public Response updateBookingPatch(int bookingId, Map<String, ?> partialFields, String token) {
        RequestSpecification request = given().spec(spec()).body(partialFields);
        if (token != null) {
            request.cookie("token", token);
        }
        return request.when().patch("/booking/{id}", bookingId);
    }

    public Response deleteBooking(int bookingId, String token) {
        RequestSpecification request = given().spec(spec());
        if (token != null) {
            request.cookie("token", token);
        }
        return request.when().delete("/booking/{id}", bookingId);
    }
}
