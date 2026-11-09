package com.entertainer.qa.clients;

import com.entertainer.qa.models.jsonplaceholder.Post;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Thin wrapper over the JSONPlaceholder (https://jsonplaceholder.typicode.com) endpoints used by
 * this suite. JSONPlaceholder is a fake-backend sandbox: writes are accepted and echoed back with
 * a plausible response but are not actually persisted server-side.
 */
public class JsonPlaceholderClient extends BaseApiClient {

    private static final String BASE_URL_CONFIG_KEY = "jsonplaceholder.base.url";

    private RequestSpecification spec() {
        return requestSpec(BASE_URL_CONFIG_KEY);
    }

    public Response getAllPosts() {
        return given().spec(spec())
                .when().get("/posts");
    }

    public Response getPost(int id) {
        return given().spec(spec())
                .when().get("/posts/{id}", id);
    }

    public Response getPostsByUserId(int userId) {
        return given().spec(spec())
                .queryParam("userId", userId)
                .when().get("/posts");
    }

    public Response createPost(Post post) {
        return given().spec(spec())
                .body(post)
                .when().post("/posts");
    }

    public Response updatePostPut(int id, Post post) {
        return given().spec(spec())
                .body(post)
                .when().put("/posts/{id}", id);
    }

    public Response updatePostPatch(int id, Map<String, ?> partialFields) {
        return given().spec(spec())
                .body(partialFields)
                .when().patch("/posts/{id}", id);
    }

    public Response deletePost(int id) {
        return given().spec(spec())
                .when().delete("/posts/{id}", id);
    }

    public Response getCommentsForPost(int postId) {
        return given().spec(spec())
                .when().get("/posts/{id}/comments", postId);
    }

    public Response getCommentsByEmail(String email) {
        return given().spec(spec())
                .queryParam("email", email)
                .when().get("/comments");
    }

    public Response getComment(int id) {
        return given().spec(spec())
                .when().get("/comments/{id}", id);
    }
}
