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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Epic("Restful-Booker API")
@Feature("Create Booking")
public class CreateBookingTests extends BaseTest {

    @Test(description = "POST /booking with a valid full payload returns 200 with a generated bookingid")
    @Severity(SeverityLevel.BLOCKER)
    public void createBooking_withValidFullPayload_returns200WithBookingId() {
        Booking booking = TestDataFactory.randomBooking();

        Response response = bookerClient.createBooking(booking);

        response.then().statusCode(200);
        CreateBookingResponse body = response.as(CreateBookingResponse.class);
        assertNotNull(body.getBookingid());
        assertEquals(body.getBooking().getFirstname(), booking.getFirstname());
    }

    @Test(description = "Create booking response matches the expected JSON schema")
    @Severity(SeverityLevel.CRITICAL)
    public void createBooking_responseSchema_matchesBookingStructure() {
        bookerClient.createBooking(TestDataFactory.randomBooking())
                .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                        "schemas/booker/create-booking-response-schema.json"));
    }

    @Test(description = "additionalneeds is optional - a booking without it is still created successfully")
    @Severity(SeverityLevel.NORMAL)
    public void createBooking_withoutOptionalAdditionalNeeds_returns200() {
        bookerClient.createBooking(TestDataFactory.bookingWithoutAdditionalNeeds())
                .then()
                .statusCode(200);
    }

    @DataProvider(name = "variedBookingPayloads")
    public Object[][] variedBookingPayloads() {
        return new Object[][]{
                {TestDataFactory.randomBooking()},
                {TestDataFactory.randomBooking()},
                {TestDataFactory.randomBooking()}
        };
    }

    @Test(dataProvider = "variedBookingPayloads",
            description = "Multiple independently generated valid bookings are each created successfully")
    @Severity(SeverityLevel.NORMAL)
    public void createBooking_withMultipleValidPayloads_returns200(Booking booking) {
        bookerClient.createBooking(booking)
                .then()
                .statusCode(200)
                .body("booking.totalprice", org.hamcrest.Matchers.equalTo(booking.getTotalprice()));
    }

    @Test(description = "A checkout date after the checkin date is accepted and persisted as sent")
    @Severity(SeverityLevel.NORMAL)
    public void createBooking_checkoutDateAfterCheckinDate_isPersisted() {
        LocalDate checkin = LocalDate.now().plusDays(5);
        LocalDate checkout = checkin.plusDays(3);
        Booking booking = Booking.builder()
                .firstname("Midhat")
                .lastname("Chughtai")
                .totalprice(250)
                .depositpaid(true)
                .bookingdates(new com.entertainer.qa.models.booker.BookingDates(checkin.toString(), checkout.toString()))
                .additionalneeds("Late checkout")
                .build();

        bookerClient.createBooking(booking)
                .then()
                .statusCode(200)
                .body("booking.bookingdates.checkin", org.hamcrest.Matchers.equalTo(checkin.toString()))
                .body("booking.bookingdates.checkout", org.hamcrest.Matchers.equalTo(checkout.toString()));
    }

    @DataProvider(name = "depositPaidValues")
    public Object[][] depositPaidValues() {
        return new Object[][]{{true}, {false}};
    }

    @Test(dataProvider = "depositPaidValues",
            description = "Both boolean values for depositpaid are accepted and reflected back")
    @Severity(SeverityLevel.MINOR)
    public void createBooking_depositPaidBooleanValues_areReflectedBack(boolean depositPaid) {
        Booking booking = TestDataFactory.randomBooking();
        booking.setDepositpaid(depositPaid);

        CreateBookingResponse body = bookerClient.createBooking(booking).as(CreateBookingResponse.class);

        assertTrue(body.getBooking().getDepositpaid() == depositPaid);
    }
}
