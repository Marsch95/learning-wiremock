package org.loonycorn.restassuredtests;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import io.restassured.RestAssured;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wiremock.RandomExtension;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.matchesRegex;

public class EmployeesApiTest {
    WireMockServer mockServer;

    @BeforeSuite
    public void setup () {
        mockServer = new WireMockServer(
                options()
                        .port(9090)
                        .globalTemplating(false)
                        .extensions(RandomExtension.class)
        );

        mockServer.start();

        RestAssured.baseURI = "http://localhost:9090";
        RestAssured.basePath = "employees";
    }

    @AfterSuite
    public void cleanup () {
        mockServer.stop();
    }

    @Test
    public void getEmployeesDetails() {
        RestAssured.given()
            .when()
                .get()
            .then()
                .statusCode(200)
                .header("Content-Type", "application/json")
                .body("size()", equalTo(2));

        mockServer.verify(1, getRequestedFor(urlEqualTo("/employees")));
    }

    @Test(dependsOnMethods = "getEmployeesDetails")
    public void getEmployee1Details() {
        RestAssured.given()
                .when()
                    .get("/1")
                .then()
                    .assertThat()
                    .statusCode(200)
                    .body("name", equalTo("John Doe"))
                    .body("position", equalTo("Software Engineer"));

        mockServer.verify(1, getRequestedFor(urlEqualTo("/employees/1")));
    }

    @Test(dependsOnMethods = "getEmployeesDetails")
    public void getEmployee2Details() {
        RestAssured.given()
                .when()
                    .get("/2")
                .then()
                    .assertThat()
                    .statusCode(200)
                    .body("name", equalTo("Jane Smith"))
                    .body("position", equalTo("Project Manager"));

        mockServer.verify(1, getRequestedFor(urlEqualTo("/employees/2")));
    }

    @Test(dependsOnMethods = "getEmployeesDetails")
    public void createEmployee() {
        String requestBodyCreate = "{ \"name\": \"Olivia Jones\", \"position\": \"Senior Director\" }";

        RestAssured.given()
                .body(requestBodyCreate)
                .when()
                    .post()
                .then()
                    .assertThat()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("name", equalTo("Olivia Jones"))
                    .body("position", equalTo("Senior Director"));

        mockServer.verify(1, postRequestedFor(urlEqualTo("/employees")));
    }

    @Test
    public void getRandomEmployeeDetails() {
        RestAssured.get("/random")
                .then()
                    .assertThat()
                    .statusCode(200)
                    .body("id.toInteger()", greaterThanOrEqualTo(1))
                    .body("name", notNullValue())
                    .body("position", anyOf(
                        equalTo("Software Engineer"),
                        equalTo("Project Manager"),
                        equalTo("Data Analyst"),
                        equalTo("UI/UX Designer"))
                    )
                    .body("createdOn", matchesRegex("\\d{2}-\\d{2}-\\d{4}T\\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    public void getEmployeeJohnDetails() {
        RestAssured.get("/John")
                .then()
                    .assertThat()
                    .statusCode(200)
                    .header("Content-Type", "application/json")
                    .body("name", equalTo("John Doe"))
                    .body("position", equalTo("Software Engineer"))
                    .log().body();
    }

    @Test
    public void getEmployeeJaneDetails() {
        RestAssured.get("/jane")
                .then()
                    .assertThat()
                    .statusCode(200)
                    .header("Content-Type", "application/json")
                    .body("name", equalTo("Jane Smith"))
                    .body("position", equalTo("Project Manager"))
                    .log().body();
    }

    @Test
    public void getEmployee1Details_withQueryParam() {
        RestAssured.given()
                    .queryParam("id", "1")
                .when()
                    .get()
                .then()
                    .assertThat()
                    .statusCode(200)
                    .header("Content-Type", "application/json")
                    .body("name", equalTo("John Doe"))
                    .body("position", equalTo("Software Engineer"))
                    .log().body();
    }

    @Test
    public void getEmployee2Details_withQueryParam() {
        RestAssured.given()
                    .queryParam("id", "2")
                .when()
                    .get()
                .then()
                    .assertThat()
                    .statusCode(200)
                    .header("Content-Type", "application/json")
                    .body("name", equalTo("Jane Smith"))
                    .body("position", equalTo("Project Manager"))
                    .log().body();
    }

}
