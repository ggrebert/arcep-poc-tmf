package fr.arcep.malfacon;

import fr.arcep.troubleticket.TroubleTicketRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class MalfaconTest {

  private static String baseUrl = "/api/malfacon";

  @Inject TroubleTicketRepository repository;

  private RequestSpecification given() {
    return RestAssured.given().basePath(baseUrl);
  }

  @BeforeEach
  public void init() {
    repository.deleteAll().await().indefinitely();
  }

  @Test
  public void testValidation() {
    given()
        .when()
        .contentType("application/json")
        .body(
            """
            {
                "name": "My trouble ticket"
            }
            """)
        .post()
        .then()
        .statusCode(400);

    given()
        .when()
        .contentType("application/json")
        .body(
            """
            {
                "mafalcon": {
                    "volumetrie": 3,
                    "localisation": "string",
                    "defaut": "string",
                    "quotePart": 0
                }
            }
            """)
        .post()
        .then()
        .statusCode(400);

    given()
        .when()
        .contentType("application/json")
        .body(
            """
            {
                "name": "My trouble ticket",
                "mafalcon": {
                    "volumetrie": 2,
                    "localisation": "string",
                    "defaut": "string",
                    "quotePart": 0
                }
            }
            """)
        .post()
        .then()
        .statusCode(400);

    given()
        .when()
        .contentType("application/json")
        .body(
            """
            {
                "name": "My trouble ticket",
                "mafalcon": {
                    "volumetrie": 4,
                    "localisation": "string",
                    "defaut": "string",
                    "quotePart": 0
                }
            }
            """)
        .post()
        .then()
        .statusCode(201);
  }
}
