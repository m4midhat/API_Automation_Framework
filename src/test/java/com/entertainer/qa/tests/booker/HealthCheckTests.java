package com.entertainer.qa.tests.booker;

import com.entertainer.qa.base.BaseTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Epic("Restful-Booker API")
@Feature("Health Check")
public class HealthCheckTests extends BaseTest {

    @Test(description = "GET /ping confirms the API is up. Documented quirk: Restful-Booker "
            + "returns 201 (not 200) for this health check")
    @Severity(SeverityLevel.CRITICAL)
    public void ping_returns201Created() {
        bookerClient.ping()
                .then()
                .statusCode(201);
    }

    @Test(description = "Ping response body is the literal text 'Created'")
    @Severity(SeverityLevel.MINOR)
    public void ping_responseBody_containsCreated() {
        Response response = bookerClient.ping();

        assertEquals(response.getBody().asString().trim(), "Created");
    }
}
