package com.entertainer.qa.tests.jsonplaceholder;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.models.jsonplaceholder.Post;
import com.entertainer.qa.utils.TestDataFactory;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.util.Map;

@Epic("JSONPlaceholder API")
@Feature("Update Post")
public class UpdatePostTests extends BaseTest {

    @Test(description = "PUT /posts/{id} fully replaces an existing post")
    @Severity(SeverityLevel.BLOCKER)
    public void updatePost_putFullUpdate_returns200() {
        Post updated = TestDataFactory.randomPost(1);
        updated.setId(1);

        jsonPlaceholderClient.updatePostPut(1, updated)
                .then()
                .statusCode(200)
                .body("title", org.hamcrest.Matchers.equalTo(updated.getTitle()));
    }

    @Test(description = "PATCH /posts/{id} partially updates an existing post")
    @Severity(SeverityLevel.CRITICAL)
    public void updatePost_patchPartialUpdate_returns200() {
        jsonPlaceholderClient.updatePostPatch(1, Map.of("title", "Patched title"))
                .then()
                .statusCode(200)
                .body("title", org.hamcrest.Matchers.equalTo("Patched title"));
    }

    @Test(description = "PUT /posts/{id} for a non-existent id returns 404")
    @Severity(SeverityLevel.NORMAL)
    public void updatePost_nonExistentId_returns404() {
        Post post = TestDataFactory.randomPost(1);
        post.setId(99_999);

        jsonPlaceholderClient.updatePostPut(99_999, post)
                .then()
                .statusCode(404);
    }
}
