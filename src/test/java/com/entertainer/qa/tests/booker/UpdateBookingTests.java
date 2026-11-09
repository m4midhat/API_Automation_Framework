package com.entertainer.qa.tests.booker;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.models.booker.Booking;
import com.entertainer.qa.models.booker.CreateBookingResponse;
import com.entertainer.qa.utils.TestDataFactory;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

@Epic("Restful-Booker API")
@Feature("Update Booking")
public class UpdateBookingTests extends BaseTest {

    private String authToken;

    @BeforeMethod(alwaysRun = true)
    public void authenticate() {
        authToken = fetchBookerAuthToken();
    }

    private int createBooking() {
        return bookerClient.createBooking(TestDataFactory.randomBooking())
                .as(CreateBookingResponse.class)
                .getBookingid();
    }

    @Test(description = "PUT /booking/{id} with a valid token fully updates the booking")
    @Severity(SeverityLevel.BLOCKER)
    public void updateBooking_putFullUpdate_withValidToken_returns200() {
        int bookingId = createBooking();
        Booking updated = TestDataFactory.randomBooking();

        Booking response = bookerClient.updateBookingPut(bookingId, updated, authToken).as(Booking.class);

        assertEquals(response.getFirstname(), updated.getFirstname());
        assertEquals(response.getTotalprice(), updated.getTotalprice());
    }

    @Test(description = "PUT /booking/{id} without a token is rejected with 403")
    @Severity(SeverityLevel.CRITICAL)
    public void updateBooking_putFullUpdate_withoutToken_returns403() {
        int bookingId = createBooking();

        bookerClient.updateBookingPut(bookingId, TestDataFactory.randomBooking(), null)
                .then()
                .statusCode(403);
    }

    @Test(description = "PATCH /booking/{id} with a valid token partially updates the booking")
    @Severity(SeverityLevel.CRITICAL)
    public void updateBooking_patchPartialUpdate_withValidToken_returns200() {
        int bookingId = createBooking();

        bookerClient.updateBookingPatch(bookingId, Map.of("firstname", "PatchedFirstName"), authToken)
                .then()
                .statusCode(200)
                .body("firstname", org.hamcrest.Matchers.equalTo("PatchedFirstName"));
    }

    @Test(description = "PATCH /booking/{id} without a token is rejected with 403")
    @Severity(SeverityLevel.CRITICAL)
    public void updateBooking_patchPartialUpdate_withoutToken_returns403() {
        int bookingId = createBooking();

        bookerClient.updateBookingPatch(bookingId, Map.of("firstname", "ShouldNotApply"), null)
                .then()
                .statusCode(403);
    }

    @Test(description = "PUT /booking/{id} with an invalid/garbage token is rejected with 403")
    @Severity(SeverityLevel.NORMAL)
    public void updateBooking_withInvalidToken_returns403() {
        int bookingId = createBooking();

        bookerClient.updateBookingPut(bookingId, TestDataFactory.randomBooking(), "not-a-real-token")
                .then()
                .statusCode(403);
    }

    @Test(description = "PUT /booking/{id} for a non-existent id returns 405 Method Not Allowed (documented quirk)")
    @Severity(SeverityLevel.NORMAL)
    public void updateBooking_nonExistentId_returns405() {
        bookerClient.updateBookingPut(999_999, TestDataFactory.randomBooking(), authToken)
                .then()
                .statusCode(405);
    }

    @DataProvider(name = "partialUpdateFields")
    public Object[][] partialUpdateFields() {
        return new Object[][]{
                {Map.of("totalprice", 777)},
                {Map.of("additionalneeds", "Late checkout, airport pickup")}
        };
    }

    @Test(dataProvider = "partialUpdateFields",
            description = "Each individually patched field is reflected back correctly")
    @Severity(SeverityLevel.MINOR)
    public void updateBooking_responseReflectsPatchedField(Map<String, ?> partialFields) {
        int bookingId = createBooking();

        var response = bookerClient.updateBookingPatch(bookingId, partialFields, authToken);

        response.then().statusCode(200);
        partialFields.forEach((field, value) ->
                response.then().body(field, org.hamcrest.Matchers.equalTo(value)));
    }
}
