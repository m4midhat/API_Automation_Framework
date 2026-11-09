package com.entertainer.qa.base;

import com.entertainer.qa.clients.BookerClient;
import com.entertainer.qa.clients.JsonPlaceholderClient;
import com.entertainer.qa.clients.ReqResClient;
import com.entertainer.qa.models.booker.AuthRequest;
import com.entertainer.qa.security.CredentialManager;
import org.testng.annotations.BeforeClass;

/**
 * Shared base for every test class: pre-wired API clients and a helper to fetch a fresh
 * Restful-Booker auth token using the decrypted credentials from {@link CredentialManager}.
 */
public abstract class BaseTest {

    protected ReqResClient reqResClient;
    protected BookerClient bookerClient;
    protected JsonPlaceholderClient jsonPlaceholderClient;

    @BeforeClass(alwaysRun = true)
    public void setUpClients() {
        reqResClient = new ReqResClient();
        bookerClient = new BookerClient();
        jsonPlaceholderClient = new JsonPlaceholderClient();
    }

    /**
     * Authenticates against Restful-Booker with the configured (encrypted-at-rest) demo
     * credentials and returns a fresh token for use on write operations.
     */
    protected String fetchBookerAuthToken() {
        String username = CredentialManager.getCredential("booker.auth.username");
        String password = CredentialManager.getCredential("booker.auth.password");

        return bookerClient.auth(new AuthRequest(username, password))
                .then().extract().path("token");
    }
}
