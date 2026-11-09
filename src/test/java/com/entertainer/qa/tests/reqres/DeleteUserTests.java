package com.entertainer.qa.tests.reqres;

import com.entertainer.qa.base.BaseTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Epic("ReqRes API")
@Feature("Delete User")
public class DeleteUserTests extends BaseTest {

    @Test(description = "DELETE /users/{id} for an existing id returns 204 No Content")
    @Severity(SeverityLevel.BLOCKER)
    public void deleteUser_existingId_returns204NoContent() {
        reqResClient.deleteUser(2)
                .then()
                .statusCode(204);
    }

    @Test(description = "DELETE /users/{id} for a non-existent id is still accepted by the sandbox (demo behaviour)")
    @Severity(SeverityLevel.NORMAL)
    public void deleteUser_nonExistentId_returns204Demo() {
        reqResClient.deleteUser(999)
                .then()
                .statusCode(204);
    }

    @Test(description = "Delete response has no body")
    @Severity(SeverityLevel.MINOR)
    public void deleteUser_responseBody_isEmpty() {
        Response response = reqResClient.deleteUser(2);

        assertEquals(response.getBody().asString(), "");
    }
}
