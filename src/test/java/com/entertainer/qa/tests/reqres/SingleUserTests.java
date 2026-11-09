package com.entertainer.qa.tests.reqres;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.models.reqres.SingleUserResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Epic("ReqRes API")
@Feature("Single User")
public class SingleUserTests extends BaseTest {

    @Test(description = "GET /users/2 returns 200 with that user's details")
    @Severity(SeverityLevel.BLOCKER)
    public void getUser_withValidId_returns200() {
        Response response = reqResClient.getUser(2);

        response.then().statusCode(200);
        assertEquals(response.as(SingleUserResponse.class).getData().getId().intValue(), 2);
    }

    @Test(description = "Single user response matches the expected JSON schema")
    @Severity(SeverityLevel.CRITICAL)
    public void getUser_responseSchema_matchesExpectedStructure() {
        reqResClient.getUser(2)
                .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/reqres/single-user-schema.json"));
    }

    @Test(description = "GET /users/23 (non-existent) returns 404 with an empty body")
    @Severity(SeverityLevel.CRITICAL)
    public void getUser_withNonExistentId_returns404WithEmptyBody() {
        Response response = reqResClient.getUser(23);

        response.then().statusCode(404);
        assertEquals(response.getBody().asString().trim(), "{}");
    }

    @Test(description = "The returned avatar is a well-formed, reachable-looking URL")
    @Severity(SeverityLevel.MINOR)
    public void getUser_avatarUrl_isReachableFormat() {
        String avatar = reqResClient.getUser(2).as(SingleUserResponse.class).getData().getAvatar();

        assertTrue(avatar.startsWith("https://") && avatar.endsWith(".jpg"),
                "Unexpected avatar URL format: " + avatar);
    }

    @DataProvider(name = "boundaryUserIds")
    public Object[][] boundaryUserIds() {
        return new Object[][]{{1}, {12}};
    }

    @Test(dataProvider = "boundaryUserIds",
            description = "First and last known user ids both resolve successfully")
    @Severity(SeverityLevel.NORMAL)
    public void getUser_withBoundaryIds_returns200(int userId) {
        reqResClient.getUser(userId)
                .then()
                .statusCode(200)
                .body("data.id", org.hamcrest.Matchers.equalTo(userId));
    }
}
