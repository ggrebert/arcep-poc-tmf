package fr.arcep.attachment;

import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AttachmentTest {

  private static String baseUrl = "/api/attachment";

  @Inject AttachmentRepository repository;

  private RequestSpecification given() {
    return RestAssured.given().basePath(baseUrl);
  }

  private String getMetadata(String filename) {
    var str =
        """
                {
                    "name": "%s"
                }
                """
            .formatted(filename);

    return str;
  }

  @BeforeEach
  public void init() {
    repository.deleteAll().await().indefinitely();
  }

  @Test
  public void testUpload() throws IOException {
    var filename = "quarkus_blogpost_formallogo.png";
    var filePath = this.getClass().getResource("/" + filename).getPath();
    var file = new File(filePath);

    given()
        .headers("X-Client-Id", "test")
        .multiPart("attachment", getMetadata(filename), "application/json")
        .multiPart("file", file, "multipart/form-data")
        .when()
        .post()
        .then()
        .statusCode(201)
        .body("name", is(filename))
        .body("mimeType", is("image/png"))
        .body("size", is(25018));
  }

  @Test
  public void testBadMimeType() throws IOException {
    var filename = "quarkus_blogpost_formallogo.png";
    var filePath = this.getClass().getResource("/" + filename).getPath();
    var file = new File(filePath);

    given()
        .headers("X-Client-Id", "test")
        .headers("X-Allowed-MimeType", "image/jpeg")
        .multiPart("attachment", getMetadata(filename), "application/json")
        .multiPart("file", file, "multipart/form-data")
        .when()
        .post()
        .then()
        .statusCode(400);
  }

  @Test
  public void testWrongMimeType() throws IOException {
    var filename = "quarkus_blogpost_formallogo.png";
    var filePath = this.getClass().getResource("/" + filename).getPath();
    var file = new File(filePath);

    given()
        .headers("X-Client-Id", "test")
        .multiPart("attachment", getMetadata("toto.jpg"), "application/json")
        .multiPart("file", file, "multipart/form-data")
        .when()
        .post()
        .then()
        .statusCode(201)
        .body("name", is("toto.jpg.png"))
        .body("mimeType", is("image/png"))
        .body("size", is(25018));
  }

  @Test
  public void testBadRequest() throws IOException {
    var filename = "quarkus_blogpost_formallogo.png";
    var filePath = this.getClass().getResource("/" + filename).getPath();
    var file = new File(filePath);

    given()
        .headers("X-Client-Id", "test")
        .multiPart("attachment", getMetadata(""), "application/json")
        .multiPart("file", file, "multipart/form-data")
        .when()
        .post()
        .then()
        .statusCode(400);
  }
}
