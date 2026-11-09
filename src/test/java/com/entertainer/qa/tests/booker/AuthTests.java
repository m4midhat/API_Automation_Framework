package com.entertainer.qa.tests.booker;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.models.booker.AuthRequest;
import com.entertainer.qa.models.booker.AuthResponse;
import com.entertainer.qa.security.CredentialManager;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Exercises /auth using the demo account stored encrypted in config/*.properties and decrypted
 * on demand via {@link CredentialManager} - the credential-encryption requirement for this
 * framework is demonstrated end-to-end through this flow.
 */
@Epic("Restful-Booker API")
@Feature("Authentication")
public class AuthTests extends BaseTest {

    @Test(description = "POST /auth with the decrypted demo credentials returns a token")
    @Severity(SeverityLevel.BLOCKER)
    public void auth_withValidCredentials_returnsToken() {
        String username = CredentialManager.getCredential("booker.auth.username");
        String password = CredentialManager.getCredential("booker.auth.password");

        Response response = bookerClient.auth(new AuthRequest(username, password));

        response.then().statusCode(200);
        assertTrue(response.as(AuthResponse.class).getToken().length() > 0);
    }

    @Test(description = "POST /auth with an invalid username returns a 'Bad credentials' reason")
    @Severity(SeverityLevel.CRITICAL)
    public void auth_withInvalidUsername_returnsBadCredentials() {
        String password = CredentialManager.getCredential("booker.auth.password");

        Response response = bookerClient.auth(new AuthRequest("not-a-real-user", password));

        response.then().statusCode(200);
        assertEquals(response.as(AuthResponse.class).getReason(), "Bad credentials");
    }

    @Test(description = "POST /auth with an invalid password returns a 'Bad credentials' reason")
    @Severity(SeverityLevel.CRITICAL)
    public void auth_withInvalidPassword_returnsBadCredentials() {
        String username = CredentialManager.getCredential("booker.auth.username");

        Response response = bookerClient.auth(new AuthRequest(username, "not-the-real-password"));

        response.then().statusCode(200);
        assertEquals(response.as(AuthResponse.class).getReason(), "Bad credentials");
    }

    @DataProvider(name = "missingFieldPayloads")
    public Object[][] missingFieldPayloads() {
        return new Object[][]{
                {"{\"password\":\"password123\"}"},
                {"{\"username\":\"admin\"}"}
        };
    }

    @Test(dataProvider = "missingFieldPayloads",
            description = "POST /auth with a missing username or password returns a 'Bad credentials' reason")
    @Severity(SeverityLevel.NORMAL)
    public void auth_withMissingField_returnsBadCredentials(String rawBody) {
        Response response = bookerClient.auth(rawBody);

        response.then().statusCode(200);
        assertEquals(response.as(AuthResponse.class).getReason(), "Bad credentials");
    }

    @Test(description = "A successful auth token is a non-empty alphanumeric string")
    @Severity(SeverityLevel.MINOR)
    public void auth_tokenFormat_isAlphanumeric() {
        String token = fetchBookerAuthToken();

        assertTrue(token.matches("^[a-zA-Z0-9]+$"), "Unexpected token format: " + token);
    }
}
