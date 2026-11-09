package com.entertainer.qa.tests.reqres;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.models.reqres.ErrorResponse;
import com.entertainer.qa.models.reqres.RegisterRequest;
import com.entertainer.qa.models.reqres.RegisterResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Epic("ReqRes API")
@Feature("Register")
public class RegisterTests extends BaseTest {

    /** The one email ReqRes's documented register flow guarantees a successful, token-bearing response for. */
    private static final String KNOWN_REGISTERABLE_EMAIL = "eve.holt@reqres.in";

    @Test(description = "POST /register with a known email + password returns 200 with id and token")
    @Severity(SeverityLevel.BLOCKER)
    public void register_withValidCredentials_returns200WithToken() {
        Response response = reqResClient.register(new RegisterRequest(KNOWN_REGISTERABLE_EMAIL, "pistol"));

        response.then().statusCode(200);
        RegisterResponse body = response.as(RegisterResponse.class);
        assertNotNull(body.getId());
        assertTrue(body.getToken() != null && !body.getToken().isEmpty());
    }

    @Test(description = "POST /register without a password returns 400 with a descriptive error")
    @Severity(SeverityLevel.CRITICAL)
    public void register_missingPassword_returns400WithError() {
        Response response = reqResClient.register("{\"email\":\"" + KNOWN_REGISTERABLE_EMAIL + "\"}");

        response.then().statusCode(400);
        assertEquals(response.as(ErrorResponse.class).getError(), "Missing password");
    }

    @Test(description = "POST /register without an email returns 400 with a descriptive error")
    @Severity(SeverityLevel.CRITICAL)
    public void register_missingEmail_returns400WithError() {
        Response response = reqResClient.register("{\"password\":\"pistol\"}");

        response.then().statusCode(400);
        assertNotNull(response.as(ErrorResponse.class).getError());
    }

    @Test(description = "The token returned on successful registration is a non-empty string")
    @Severity(SeverityLevel.MINOR)
    public void register_tokenFormat_isNonEmptyString() {
        String token = reqResClient.register(new RegisterRequest(KNOWN_REGISTERABLE_EMAIL, "pistol"))
                .as(RegisterResponse.class)
                .getToken();

        assertTrue(token.length() > 0, "token should not be empty");
    }
}
