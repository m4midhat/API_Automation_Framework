package com.entertainer.qa.tests.jsonplaceholder;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.models.jsonplaceholder.Post;
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

@Epic("JSONPlaceholder API")
@Feature("Create Post")
public class CreatePostTests extends BaseTest {

    @Test(description = "POST /posts with a valid payload returns 201 with a generated id")
    @Severity(SeverityLevel.BLOCKER)
    public void createPost_withValidPayload_returns201WithId() {
        Post post = TestDataFactory.randomPost(1);

        Response response = jsonPlaceholderClient.createPost(post);

        response.then().statusCode(201);
        assertNotNull(response.as(Post.class).getId());
    }

    @Test(description = "The create response echoes back the request body fields")
    @Severity(SeverityLevel.CRITICAL)
    public void createPost_responseEchoesRequestBody() {
        Post post = TestDataFactory.randomPost(2);

        Post created = jsonPlaceholderClient.createPost(post).as(Post.class);

        assertEquals(created.getTitle(), post.getTitle());
        assertEquals(created.getBody(), post.getBody());
        assertEquals(created.getUserId(), post.getUserId());
    }

    @DataProvider(name = "postsForDifferentUsers")
    public Object[][] postsForDifferentUsers() {
        return new Object[][]{
                {TestDataFactory.randomPost(1)},
                {TestDataFactory.randomPost(5)},
                {TestDataFactory.randomPost(10)}
        };
    }

    @Test(dataProvider = "postsForDifferentUsers",
            description = "Posts created for different userIds are all accepted")
    @Severity(SeverityLevel.NORMAL)
    public void createPost_forDifferentUsers_returns201(Post post) {
        jsonPlaceholderClient.createPost(post)
                .then()
                .statusCode(201)
                .body("userId", org.hamcrest.Matchers.equalTo(post.getUserId()));
    }
}
