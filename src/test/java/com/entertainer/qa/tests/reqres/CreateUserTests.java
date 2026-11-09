package com.entertainer.qa.tests.reqres;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.models.reqres.CreateUserRequest;
import com.entertainer.qa.models.reqres.CreateUserResponse;
import com.entertainer.qa.utils.TestDataFactory;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

@Epic("ReqRes API")
@Feature("Create User")
public class CreateUserTests extends BaseTest {

    @DataProvider(name = "validUserPayloads")
    public Object[][] validUserPayloads() {
        return new Object[][]{
                {TestDataFactory.userWith("qa-lead", "SDET")},
                {TestDataFactory.userWith("automation-engineer", "QA Automation")},
                {TestDataFactory.userWith("program-architect", "Quality Engineering Leader")}
        };
    }

    @Test(dataProvider = "validUserPayloads",
            description = "POST /users with a valid payload returns 201 with a generated id and createdAt")
    @Severity(SeverityLevel.BLOCKER)
    public void createUser_withValidPayload_returns201WithIdAndTimestamp(CreateUserRequest request) {
        Response response = reqResClient.createUser(request);

        response.then().statusCode(201);
        CreateUserResponse body = response.as(CreateUserResponse.class);

        assertEquals(body.getName(), request.getName());
        assertEquals(body.getJob(), request.getJob());
        assertNotNull(body.getId(), "id should be generated");
        assertNotNull(body.getCreatedAt(), "createdAt should be generated");
    }

    @Test(description = "POST /users with an empty body is still accepted by the sandbox (demo behaviour)")
    @Severity(SeverityLevel.NORMAL)
    public void createUser_withEmptyBody_returns201Demo() {
        reqResClient.createUser("{}")
                .then()
                .statusCode(201)
                .body("id", org.hamcrest.Matchers.notNullValue())
                .body("createdAt", org.hamcrest.Matchers.notNullValue());
    }

    @Test(description = "Create user response is returned as application/json")
    @Severity(SeverityLevel.MINOR)
    public void createUser_responseContentType_isJson() {
        reqResClient.createUser(TestDataFactory.randomUser())
                .then()
                .contentType(ContentType.JSON);
    }

    @Test(description = "createdAt is a valid ISO-8601 timestamp")
    @Severity(SeverityLevel.MINOR)
    public void createUser_createdTimestamp_isValidIsoFormat() {
        String createdAt = reqResClient.createUser(TestDataFactory.randomUser())
                .as(CreateUserResponse.class)
                .getCreatedAt();

        try {
            OffsetDateTime.parse(createdAt);
        } catch (DateTimeParseException e) {
            fail("createdAt was not a valid ISO-8601 timestamp: " + createdAt);
        }
    }
}
