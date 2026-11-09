package com.entertainer.qa.tests.reqres;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.config.ConfigManager;
import com.entertainer.qa.models.reqres.ListUsersResponse;
import com.entertainer.qa.models.reqres.UserData;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.lessThan;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Epic("ReqRes API")
@Feature("List Users")
public class ListUsersTests extends BaseTest {

    @Test(description = "GET /users?page=2 returns 200 with correct pagination metadata")
    @Severity(SeverityLevel.BLOCKER)
    public void listUsers_withValidPage_returns200WithPaginationMeta() {
        Response response = reqResClient.listUsers(2);

        response.then().statusCode(200);
        ListUsersResponse body = response.as(ListUsersResponse.class);

        assertEquals(body.getPage().intValue(), 2);
        assertTrue(body.getTotal() > 0, "total should be greater than 0");
    }

    @Test(description = "List users response matches the expected JSON schema")
    @Severity(SeverityLevel.CRITICAL)
    public void listUsers_responseSchema_matchesExpectedStructure() {
        reqResClient.listUsers(1)
                .then()
                .statusCode(200)
                .body(org.hamcrest.Matchers.notNullValue())
                .assertThat()
                .body(io.restassured.module.jsv.JsonSchemaValidator
                        .matchesJsonSchemaInClasspath("schemas/reqres/list-users-schema.json"));
    }

    @Test(description = "per_page matches the actual number of items returned")
    @Severity(SeverityLevel.NORMAL)
    public void listUsers_perPage_matchesActualDataCount() {
        ListUsersResponse body = reqResClient.listUsers(1).as(ListUsersResponse.class);

        assertEquals(body.getData().size(), body.getPerPage().intValue());
    }

    @Test(description = "total_pages is consistent with total and per_page")
    @Severity(SeverityLevel.NORMAL)
    public void listUsers_totalPages_isConsistentWithTotalAndPerPage() {
        ListUsersResponse body = reqResClient.listUsers(1).as(ListUsersResponse.class);

        int expectedTotalPages = (int) Math.ceil((double) body.getTotal() / body.getPerPage());
        assertEquals(body.getTotalPages().intValue(), expectedTotalPages);
    }

    @Test(description = "Requesting a page beyond the known range returns an empty data array, not an error")
    @Severity(SeverityLevel.NORMAL)
    public void listUsers_pageBeyondRange_returnsEmptyDataArray() {
        ListUsersResponse body = reqResClient.listUsers(999).as(ListUsersResponse.class);

        assertTrue(body.getData().isEmpty(), "data should be empty for an out-of-range page");
    }

    @Test(description = "Every returned user has a syntactically valid @reqres.in email")
    @Severity(SeverityLevel.MINOR)
    public void listUsers_everyUser_hasValidEmailFormat() {
        List<UserData> users = reqResClient.listUsers(1).as(ListUsersResponse.class).getData();

        for (UserData user : users) {
            assertTrue(user.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"),
                    "Invalid email format: " + user.getEmail());
        }
    }

    @Test(description = "Every returned user has a unique id within the page")
    @Severity(SeverityLevel.MINOR)
    public void listUsers_everyUser_hasUniqueId() {
        List<UserData> users = reqResClient.listUsers(1).as(ListUsersResponse.class).getData();

        Set<Integer> ids = new HashSet<>();
        for (UserData user : users) {
            assertTrue(ids.add(user.getId()), "Duplicate id found: " + user.getId());
        }
    }

    @Test(description = "List users responds within the agreed SLA threshold")
    @Severity(SeverityLevel.MINOR)
    public void listUsers_responseTime_isBelowSlaThreshold() {
        long slaThresholdMs = ConfigManager.getInt("http.sla.max.response.time.ms", 5000);

        reqResClient.listUsers(1)
                .then()
                .time(lessThan(slaThresholdMs));
    }
}
