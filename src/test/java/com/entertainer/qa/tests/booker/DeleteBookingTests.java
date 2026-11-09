package com.entertainer.qa.tests.booker;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.models.booker.CreateBookingResponse;
import com.entertainer.qa.utils.TestDataFactory;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Epic("Restful-Booker API")
@Feature("Delete Booking")
public class DeleteBookingTests extends BaseTest {

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

    @Test(description = "DELETE /booking/{id} with a valid token returns 201 and removes the booking")
    @Severity(SeverityLevel.BLOCKER)
    public void deleteBooking_withValidToken_returns201() {
        int bookingId = createBooking();

        bookerClient.deleteBooking(bookingId, authToken)
                .then()
                .statusCode(201);

        bookerClient.getBooking(bookingId)
                .then()
                .statusCode(404);
    }

    @Test(description = "DELETE /booking/{id} without a token is rejected with 403")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteBooking_withoutToken_returns403() {
        int bookingId = createBooking();

        bookerClient.deleteBooking(bookingId, null)
                .then()
                .statusCode(403);
    }

    @Test(description = "DELETE /booking/{id} with an invalid token is rejected with 403")
    @Severity(SeverityLevel.NORMAL)
    public void deleteBooking_withInvalidToken_returns403() {
        int bookingId = createBooking();

        bookerClient.deleteBooking(bookingId, "not-a-real-token")
                .then()
                .statusCode(403);
    }

    @Test(description = "DELETE /booking/{id} for a non-existent id returns 405 Method Not Allowed (documented quirk)")
    @Severity(SeverityLevel.NORMAL)
    public void deleteBooking_nonExistentId_returns405() {
        bookerClient.deleteBooking(999_999, authToken)
                .then()
                .statusCode(405);
    }
}
