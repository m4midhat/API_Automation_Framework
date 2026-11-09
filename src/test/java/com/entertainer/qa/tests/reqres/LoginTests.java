package com.entertainer.qa.tests.reqres;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.models.reqres.ErrorResponse;
import com.entertainer.qa.models.reqres.LoginRequest;
import com.entertainer.qa.models.reqres.LoginResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Epic("ReqRes API")
@Feature("Login")
public class LoginTests extends BaseTest {

    private static final String KNOWN_LOGIN_EMAIL = "eve.holt@reqres.in";
    private static final String KNOWN_LOGIN_PASSWORD = "cityslicka";

    @Test(description = "POST /login with valid credentials returns 200 with a token")
    @Severity(SeverityLevel.BLOCKER)
    public void login_withValidCredentials_returns200WithToken() {
        Response response = reqResClient.login(new LoginRequest(KNOWN_LOGIN_EMAIL, KNOWN_LOGIN_PASSWORD));

        response.then().statusCode(200);
        assertTrue(response.as(LoginResponse.class).getToken().length() > 0);
    }

    @Test(description = "POST /login without a password returns 400 with a descriptive error")
    @Severity(SeverityLevel.CRITICAL)
    public void login_missingPassword_returns400WithError() {
        Response response = reqResClient.login("{\"email\":\"" + KNOWN_LOGIN_EMAIL + "\"}");

        response.then().statusCode(400);
        assertEquals(response.as(ErrorResponse.class).getError(), "Missing password");
    }

    @Test(description = "POST /login without an email returns 400 with a descriptive error")
    @Severity(SeverityLevel.CRITICAL)
    public void login_missingEmail_returns400WithError() {
        Response response = reqResClient.login("{\"password\":\"" + KNOWN_LOGIN_PASSWORD + "\"}");

        response.then().statusCode(400);
        assertEquals(response.as(ErrorResponse.class).getError(), "Missing email or username");
    }

    @Test(description = "The token returned on successful login is a non-empty string")
    @Severity(SeverityLevel.MINOR)
    public void login_tokenFormat_isNonEmptyString() {
        String token = reqResClient.login(new LoginRequest(KNOWN_LOGIN_EMAIL, KNOWN_LOGIN_PASSWORD))
                .as(LoginResponse.class)
                .getToken();

        assertTrue(token != null && !token.isEmpty());
    }
}
