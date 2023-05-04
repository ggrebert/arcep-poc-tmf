package fr.arcep.malfacon;

import static io.restassured.RestAssured.given;

import fr.arcep.troubleticket.TroubleTicketRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class MalfaconTest {

  private static String baseUrl = "/api/malfacon";

  @Inject TroubleTicketRepository repository;

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
        .post(baseUrl)
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
        .post(baseUrl)
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
        .post(baseUrl)
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
        .post(baseUrl)
        .then()
        .statusCode(201);
  }
}
