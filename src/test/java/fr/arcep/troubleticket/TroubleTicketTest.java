package fr.arcep.troubleticket;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TroubleTicketTest {

  private static String baseUrl = "/api/troubleTicket";

  @Inject TroubleTicketRepository repository;

  private RequestSpecification given() {
    return RestAssured.given().basePath(baseUrl);
  }

  @BeforeEach
  public void init() {
    repository.deleteAll().await().indefinitely();
  }

  @Test
  public void testList() {
    given().when().headers("X-Client-Id", "test").get().then().statusCode(206).body(is("[]"));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .head()
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
        .post()
        .then()
        .statusCode(201)
        .body("name", is("test trouble ticket"));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .get()
        .then()
        .statusCode(206)
        .header("X-Total-Count", is("1"))
        .body("size()", is(1))
        .body("[0].name", is("test trouble ticket"));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .queryParam("limit", "0")
        .get()
        .then()
        .statusCode(206)
        .header("X-Total-Count", is("1"))
        .body("size()", is(0));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .head()
        .then()
        .statusCode(204)
        .header("X-Total-Count", is("1"));

    given()
        .when()
        .contentType("application/json")
        .headers("X-Client-Id", "test")
        .body(
            """
            {
                "name": "other trouble ticket"
            }
            """)
        .post()
        .then()
        .statusCode(201)
        .body("name", is("other trouble ticket"));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .head()
        .then()
        .statusCode(204)
        .header("X-Total-Count", is("2"));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .queryParam("name", "other trouble ticket")
        .get()
        .then()
        .statusCode(206)
        .header("X-Total-Count", is("1"))
        .body("[0].name", is("other trouble ticket"));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .queryParam("name[=~]", "^OTHER")
        .queryParam("creationDate[gt]", "2021-01-01")
        .get()
        .then()
        .statusCode(206)
        .header("X-Total-Count", is("1"))
        .body("[0].name", is("other trouble ticket"));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .queryParam("description[is]", "null")
        .queryParam("sort", "creationDate")
        .queryParam("closed", "false")
        .get()
        .then()
        .statusCode(206)
        .header("X-Total-Count", is("2"))
        .body("[0].name", is("test trouble ticket"))
        .body("[1].name", is("other trouble ticket"));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .queryParam("description[is]", "null")
        .queryParam("sort", "-creationDate")
        .get()
        .then()
        .statusCode(206)
        .header("X-Total-Count", is("2"))
        .body("[0].name", is("other trouble ticket"))
        .body("[1].name", is("test trouble ticket"));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .queryParam("fields", "name")
        .queryParam("sort", "-creationDate")
        .get()
        .then()
        .statusCode(206)
        .header("X-Total-Count", is("2"))
        .body(is("[{\"name\":\"other trouble ticket\"},{\"name\":\"test trouble ticket\"}]"));
  }

  @Test
  public void testFullLifeCycle() throws IOException {
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
            .post()
            .then()
            .statusCode(201)
            .body("name", is("test trouble ticket"))
            .extract()
            .path("id");

    given()
        .when()
        .headers("X-Client-Id", "test")
        .queryParam("fields", "")
        .get()
        .then()
        .statusCode(206)
        .body("size()", is(1))
        .body("[0].name", is("test trouble ticket"));

    var stream =
        given()
            .when()
            .accept("text/event-stream")
            .headers("X-Client-Id", "test")
            .queryParam("fields", "name")
            .get()
            .asInputStream();

    assertEquals(
        "data:{\"name\":\"test trouble ticket\"}\n\n",
        new String(stream.readAllBytes(), StandardCharsets.UTF_8));

    given()
        .when()
        .headers("X-Client-Id", "test")
        .get(id)
        .then()
        .statusCode(200)
        .body("name", is("test trouble ticket"));

    given().when().headers("X-Client-Id", "test").delete(id).then().statusCode(204);

    given().when().headers("X-Client-Id", "test").get().then().statusCode(206).body(is("[]"));
  }

  @Test
  public void testDomainRequirement() {
    given().when().get().then().statusCode(401);

    given().when().headers("X-Client-Id", "").get().then().statusCode(401);

    given()
        .when()
        .contentType("application/json")
        .body(
            """
            {
                "name": "test trouble ticket"
            }
            """)
        .post()
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
            .post()
            .then()
            .statusCode(201)
            .body("name", is("test trouble ticket"))
            .extract()
            .path("id");

    given().when().headers("X-Client-Id", "toto").get().then().statusCode(206).body(is("[]"));

    given().when().headers("X-Client-Id", "toto").get(id).then().statusCode(404);

    given()
        .when()
        .headers("X-Client-Id", "admin")
        .get()
        .then()
        .statusCode(206)
        .body("size()", is(1));

    given().when().headers("X-Client-Id", "admin").get(id).then().statusCode(200);

    given().when().headers("X-Client-Id", "admin").delete(id).then().statusCode(404);
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
        .post()
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
        .post()
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
        .post()
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
        .post()
        .then()
        .statusCode(201)
        .body("name", is("test trouble ticket"))
        .body("domain", is("doudou"));
  }
}
