package com.entertainer.qa.tests.reqres;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.models.reqres.CreateUserRequest;
import com.entertainer.qa.models.reqres.UpdateUserResponse;
import com.entertainer.qa.utils.TestDataFactory;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Epic("ReqRes API")
@Feature("Update User")
public class UpdateUserTests extends BaseTest {

    @Test(description = "PUT /users/{id} fully updates the resource and returns a fresh updatedAt")
    @Severity(SeverityLevel.BLOCKER)
    public void updateUser_putFullUpdate_returns200WithUpdatedTimestamp() {
        CreateUserRequest request = TestDataFactory.userWith("qa-lead", "Senior SDET");

        Response response = reqResClient.updateUserPut(2, request);

        response.then().statusCode(200);
        UpdateUserResponse body = response.as(UpdateUserResponse.class);
        assertEquals(body.getName(), request.getName());
        assertNotNull(body.getUpdatedAt());
    }

    @Test(description = "PATCH /users/{id} partially updates the resource")
    @Severity(SeverityLevel.CRITICAL)
    public void updateUser_patchPartialUpdate_returns200() {
        CreateUserRequest request = new CreateUserRequest(null, "Programme Architect");

        reqResClient.updateUserPatch(2, request)
                .then()
                .statusCode(200)
                .body("job", org.hamcrest.Matchers.equalTo("Programme Architect"));
    }

    @Test(description = "Updating a non-existent id is still accepted by the sandbox (demo behaviour)")
    @Severity(SeverityLevel.NORMAL)
    public void updateUser_nonExistentId_stillReturns200Demo() {
        reqResClient.updateUserPut(999, TestDataFactory.randomUser())
                .then()
                .statusCode(200);
    }

    @DataProvider(name = "updatePayloads")
    public Object[][] updatePayloads() {
        return new Object[][]{
                {TestDataFactory.userWith("qa-lead", "Principal Test Consultant")},
                {TestDataFactory.userWith("qa-lead", "QA Manager")}
        };
    }

    @Test(dataProvider = "updatePayloads",
            description = "The update response echoes back exactly what was sent")
    @Severity(SeverityLevel.NORMAL)
    public void updateUser_responseReflectsRequestBody(CreateUserRequest request) {
        UpdateUserResponse body = reqResClient.updateUserPut(2, request).as(UpdateUserResponse.class);

        assertEquals(body.getName(), request.getName());
        assertEquals(body.getJob(), request.getJob());
    }
}
