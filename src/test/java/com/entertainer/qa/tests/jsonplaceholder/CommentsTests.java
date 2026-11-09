package com.entertainer.qa.tests.jsonplaceholder;

import com.entertainer.qa.base.BaseTest;
import com.entertainer.qa.models.jsonplaceholder.Comment;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Epic("JSONPlaceholder API")
@Feature("Comments")
public class CommentsTests extends BaseTest {

    @Test(description = "GET /posts/{id}/comments returns a non-empty array of comments")
    @Severity(SeverityLevel.BLOCKER)
    public void getCommentsForPost_returnsNonEmptyArray() {
        Response response = jsonPlaceholderClient.getCommentsForPost(1);

        response.then().statusCode(200);
        List<Comment> comments = List.of(response.as(Comment[].class));
        assertFalse(comments.isEmpty());
    }

    @Test(description = "Every comment returned for a post references that same postId")
    @Severity(SeverityLevel.CRITICAL)
    public void getCommentsForPost_allCommentsReferenceCorrectPostId() {
        List<Comment> comments = List.of(jsonPlaceholderClient.getCommentsForPost(1).as(Comment[].class));

        assertTrue(comments.stream().allMatch(comment -> comment.getPostId() == 1));
    }

    @Test(description = "GET /comments?email= filters comments by the exact email address")
    @Severity(SeverityLevel.NORMAL)
    public void getCommentsByEmail_queryParam_returnsFilteredResults() {
        String knownEmail = "Eliseo@gardner.biz";

        List<Comment> comments = List.of(jsonPlaceholderClient.getCommentsByEmail(knownEmail).as(Comment[].class));

        assertFalse(comments.isEmpty());
        assertTrue(comments.stream().allMatch(comment -> comment.getEmail().equalsIgnoreCase(knownEmail)));
    }

    @Test(description = "GET /comments/{id} for a valid id returns that comment's details")
    @Severity(SeverityLevel.NORMAL)
    public void getCommentById_validId_returnsDetails() {
        jsonPlaceholderClient.getComment(1)
                .then()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.equalTo(1))
                .body("postId", org.hamcrest.Matchers.notNullValue());
    }

    @Test(description = "Single comment response matches the expected JSON schema")
    @Severity(SeverityLevel.MINOR)
    public void getCommentById_responseSchema_matchesCommentStructure() {
        jsonPlaceholderClient.getComment(1)
                .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/jsonplaceholder/comment-schema.json"));
    }

    @Test(description = "GET /comments/{id} for a non-existent id returns 404")
    @Severity(SeverityLevel.NORMAL)
    public void getCommentById_nonExistentId_returns404() {
        jsonPlaceholderClient.getComment(99_999)
                .then()
                .statusCode(404);
    }
}
