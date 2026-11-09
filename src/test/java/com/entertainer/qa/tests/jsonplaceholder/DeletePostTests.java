package com.entertainer.qa.tests.jsonplaceholder;

import com.entertainer.qa.base.BaseTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Epic("JSONPlaceholder API")
@Feature("Delete Post")
public class DeletePostTests extends BaseTest {

    @Test(description = "DELETE /posts/{id} for an existing id returns 200")
    @Severity(SeverityLevel.BLOCKER)
    public void deletePost_existingId_returns200() {
        jsonPlaceholderClient.deletePost(1)
                .then()
                .statusCode(200);
    }

    @Test(description = "Delete response body is an empty JSON object")
    @Severity(SeverityLevel.MINOR)
    public void deletePost_responseBody_isEmptyObject() {
        Response response = jsonPlaceholderClient.deletePost(1);

        assertEquals(response.getBody().asString().trim(), "{}");
    }
}
