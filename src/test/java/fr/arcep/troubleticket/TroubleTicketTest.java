package fr.arcep.troubleticket;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TroubleTicketTest {

  private static String baseUrl = "/api/troubleTicket";

  @Inject TroubleTicketRepository repository;

  @BeforeEach
  public void init() {
    repository.deleteAll().await().indefinitely();
  }

  @Test
  public void testList() {
    given()
        .when()
        .headers("X-Client-Id", "test")
        .get(baseUrl)
        .then()
        .statusCode(206)
        .body(is("[]"));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .head(baseUrl)
        .then()
        .statusCode(204)
        .header("X-Total-Count", is("0"));

    given()
        .when()
        .contentType("application/json")
        .headers("X-Client-Id", "test")
        .body(
            """
            {
                "name": "test trouble ticket"
            }
            """)
        .post(baseUrl)
        .then()
        .statusCode(201)
        .body("name", is("test trouble ticket"))
        .extract()
        .path("id");

    given()
        .when()
        .headers("X-Client-Id", "test")
        .get(baseUrl)
        .then()
        .statusCode(206)
        .body("size()", is(1))
        .body("[0].name", is("test trouble ticket"));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .head(baseUrl)
        .then()
        .statusCode(204)
        .header("X-Total-Count", is("1"));
  }

  @Test
  public void testFullLifeCycle() {
    String id =
        given()
            .when()
            .contentType("application/json")
            .headers("X-Client-Id", "test")
            .body(
                """
                {
                    "name": "test trouble ticket"
                }
                """)
            .post(baseUrl)
            .then()
            .statusCode(201)
            .body("name", is("test trouble ticket"))
            .extract()
            .path("id");

    given()
        .when()
        .headers("X-Client-Id", "test")
        .get(baseUrl)
        .then()
        .statusCode(206)
        .body("size()", is(1))
        .body("[0].name", is("test trouble ticket"));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .get(baseUrl + "/" + id)
        .then()
        .statusCode(200)
        .body("name", is("test trouble ticket"));

    given().when().headers("X-Client-Id", "test").delete(baseUrl + "/" + id).then().statusCode(204);

    given()
        .when()
        .headers("X-Client-Id", "test")
        .get(baseUrl)
        .then()
        .statusCode(206)
        .body(is("[]"));
  }

  @Test
  public void testDomainRequirement() {
    given().when().get(baseUrl).then().statusCode(401);

    given()
        .when()
        .contentType("application/json")
        .body(
            """
            {
                "name": "test trouble ticket"
            }
            """)
        .post(baseUrl)
        .then()
        .statusCode(401);
  }

  @Test
  public void testDomainAuthorization() {
    String id =
        given()
            .when()
            .contentType("application/json")
            .headers("X-Client-Id", "test")
            .body(
                """
                {
                    "name": "test trouble ticket"
                }
                """)
            .post(baseUrl)
            .then()
            .statusCode(201)
            .body("name", is("test trouble ticket"))
            .extract()
            .path("id");

    given()
        .when()
        .headers("X-Client-Id", "toto")
        .get(baseUrl)
        .then()
        .statusCode(206)
        .body(is("[]"));

    given().when().headers("X-Client-Id", "toto").get(baseUrl + "/" + id).then().statusCode(404);
  }

  @Test
  public void testValidation() {
    given()
        .when()
        .contentType("application/json")
        .headers("X-Client-Id", "test")
        .body("""
            {
                "name": ""
            }
            """)
        .post(baseUrl)
        .then()
        .statusCode(400);
  }

  @Test
  public void testCustomProperty() {
    given()
        .when()
        .contentType("application/json")
        .headers("X-Client-Id", "test")
        .body(
            """
            {
                "name": "test trouble ticket",
                "doudou": {
                    "toto": "titi"
                }
            }
            """)
        .post(baseUrl)
        .then()
        .statusCode(201)
        .body("name", is("test trouble ticket"))
        .body("doudou.toto", is("titi"));
  }

  @Test
  public void testReadOnlyProperty() {
    given()
        .when()
        .contentType("application/json")
        .headers("X-Client-Id", "test")
        .body(
            """
            {
                "name": "test trouble ticket",
                "closed": true
            }
            """)
        .post(baseUrl)
        .then()
        .statusCode(201)
        .body("name", is("test trouble ticket"))
        .body("closed", is(false));
  }

  @Test
  public void testSpecialFields() {
    given()
        .when()
        .contentType("application/json")
        .headers("X-Client-Id", "test")
        .body(
            """
            {
                "name": "test trouble ticket",
                "domain": "doudou"
            }
            """)
        .post(baseUrl)
        .then()
        .statusCode(201)
        .body("name", is("test trouble ticket"))
        .body("domain", is("doudou"));
  }
}
