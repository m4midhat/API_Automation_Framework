package com.entertainer.qa.tests.jsonplaceholder;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.models.jsonplaceholder.Post;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Epic("JSONPlaceholder API")
@Feature("Get Posts")
public class GetPostsTests extends BaseTest {

    @Test(description = "GET /posts returns all 100 seeded posts")
    @Severity(SeverityLevel.BLOCKER)
    public void getAllPosts_returns200WithExpectedCount() {
        Response response = jsonPlaceholderClient.getAllPosts();

        response.then().statusCode(200);
        List<Post> posts = List.of(response.as(Post[].class));
        assertEquals(posts.size(), 100);
    }

    @Test(description = "GET /posts/{id} for a valid id returns that post's details")
    @Severity(SeverityLevel.CRITICAL)
    public void getPostById_validId_returnsPostDetails() {
        jsonPlaceholderClient.getPost(1)
                .then()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.equalTo(1))
                .body("userId", org.hamcrest.Matchers.notNullValue())
                .body("title", org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyString()));
    }

    @Test(description = "GET /posts/{id} for a non-existent id returns 404")
    @Severity(SeverityLevel.CRITICAL)
    public void getPostById_nonExistentId_returns404() {
        jsonPlaceholderClient.getPost(99_999)
                .then()
                .statusCode(404);
    }

    @Test(description = "GET /posts?userId= returns only posts belonging to that user")
    @Severity(SeverityLevel.NORMAL)
    public void getPostsByUserId_returnsFilteredSubset() {
        Response response = jsonPlaceholderClient.getPostsByUserId(1);

        response.then().statusCode(200);
        List<Post> posts = List.of(response.as(Post[].class));
        assertTrue(!posts.isEmpty() && posts.stream().allMatch(post -> post.getUserId() == 1));
    }

    @Test(description = "Single post response matches the expected JSON schema")
    @Severity(SeverityLevel.NORMAL)
    public void getPostById_responseSchema_matchesPostStructure() {
        jsonPlaceholderClient.getPost(1)
                .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/jsonplaceholder/post-schema.json"));
    }

    @DataProvider(name = "boundaryPostIds")
    public Object[][] boundaryPostIds() {
        return new Object[][]{{1}, {100}};
    }

    @Test(dataProvider = "boundaryPostIds",
            description = "The first and last seeded post ids both resolve successfully")
    @Severity(SeverityLevel.MINOR)
    public void getPostById_boundaryIds_returns200(int postId) {
        jsonPlaceholderClient.getPost(postId)
                .then()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.equalTo(postId));
    }
}
