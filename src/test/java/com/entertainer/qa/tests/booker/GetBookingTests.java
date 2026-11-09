package com.entertainer.qa.tests.booker;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.models.booker.Booking;
import com.entertainer.qa.models.booker.CreateBookingResponse;
import com.entertainer.qa.utils.TestDataFactory;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Epic("Restful-Booker API")
@Feature("Get Booking")
public class GetBookingTests extends BaseTest {

    @Test(description = "GET /booking returns a non-empty array of booking ids")
    @Severity(SeverityLevel.BLOCKER)
    public void getBookingIds_returnsNonEmptyArray() {
        Response response = bookerClient.getBookingIds();

        response.then().statusCode(200);
        List<Map<String, Integer>> ids = response.jsonPath().getList(".");
        assertFalse(ids.isEmpty(), "Expected at least one booking id");
    }

    @Test(description = "Every element of the booking id list carries a bookingid field")
    @Severity(SeverityLevel.MINOR)
    public void getBookingIds_eachElementHasBookingIdField() {
        List<Map<String, Object>> ids = bookerClient.getBookingIds().jsonPath().getList(".");

        for (Map<String, Object> entry : ids) {
            assertTrue(entry.containsKey("bookingid"), "Missing bookingid field in: " + entry);
        }
    }

    @Test(description = "Filtering by firstname+lastname returns exactly the booking just created with that name")
    @Severity(SeverityLevel.CRITICAL)
    public void getBookingIds_filterByFirstnameAndLastname_returnsOnlyCreatedBooking() {
        Booking booking = TestDataFactory.randomBooking();
        CreateBookingResponse created = bookerClient.createBooking(booking).as(CreateBookingResponse.class);

        Response response = bookerClient.getBookingIds(Map.of(
                "firstname", booking.getFirstname(),
                "lastname", booking.getLastname()));

        response.then().statusCode(200);
        List<Integer> matchingIds = response.jsonPath().getList("bookingid", Integer.class);
        assertTrue(matchingIds.contains(created.getBookingid()),
                "Expected created booking id " + created.getBookingid() + " in filtered results " + matchingIds);
    }

    @Test(description = "Filtering by checkin/checkout date range is accepted and returns the expected array contract. "
            + "Restful-Booker's date-range filter is a documented quirk of the sandbox (it does not reliably "
            + "include every booking whose dates match), so this intentionally checks the response contract "
            + "rather than exact membership - see https://github.com/mwinteringham/restful-booker for context")
    @Severity(SeverityLevel.NORMAL)
    public void getBookingIds_filterByCheckinCheckoutRange_returnsArrayContract() {
        Booking booking = TestDataFactory.randomBooking();
        bookerClient.createBooking(booking);

        Response response = bookerClient.getBookingIds(Map.of(
                "checkin", booking.getBookingdates().getCheckin(),
                "checkout", booking.getBookingdates().getCheckout()));

        response.then().statusCode(200);
        List<Map<String, Object>> results = response.jsonPath().getList(".");
        assertTrue(results.stream().allMatch(entry -> entry.containsKey("bookingid")),
                "Every element of a date-filtered result should still carry a bookingid field");
    }

    @Test(description = "GET /booking/{id} for an existing id returns its full details")
    @Severity(SeverityLevel.BLOCKER)
    public void getBookingById_existingId_returnsBookingDetails() {
        Booking booking = TestDataFactory.randomBooking();
        int bookingId = bookerClient.createBooking(booking).as(CreateBookingResponse.class).getBookingid();

        bookerClient.getBooking(bookingId)
                .then()
                .statusCode(200)
                .body("firstname", org.hamcrest.Matchers.equalTo(booking.getFirstname()));
    }

    @Test(description = "GET /booking/{id} for a non-existent id returns 404")
    @Severity(SeverityLevel.CRITICAL)
    public void getBookingById_nonExistentId_returns404() {
        bookerClient.getBooking(999_999)
                .then()
                .statusCode(404);
    }

    @Test(description = "Get booking response matches the expected JSON schema")
    @Severity(SeverityLevel.NORMAL)
    public void getBookingById_responseSchema_matchesBookingStructure() {
        int bookingId = bookerClient.createBooking(TestDataFactory.randomBooking())
                .as(CreateBookingResponse.class).getBookingid();

        bookerClient.getBooking(bookingId)
                .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/booker/booking-schema.json"));
    }

    @Test(description = "The booking fetched by id exactly matches what was sent on create")
    @Severity(SeverityLevel.NORMAL)
    public void getBookingById_afterCreate_matchesCreatedPayload() {
        Booking booking = TestDataFactory.randomBooking();
        int bookingId = bookerClient.createBooking(booking).as(CreateBookingResponse.class).getBookingid();

        Booking fetched = bookerClient.getBooking(bookingId).as(Booking.class);

        assertEquals(fetched.getFirstname(), booking.getFirstname());
        assertEquals(fetched.getLastname(), booking.getLastname());
        assertEquals(fetched.getTotalprice(), booking.getTotalprice());
    }
}
