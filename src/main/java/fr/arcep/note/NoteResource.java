package fr.arcep.note;

import fr.arcep.OpenAPI;
import fr.arcep.tmf.model.Note;
import fr.arcep.tmf.model.params.PaginateQuery;
import fr.arcep.tmf.util.TmfApiBase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestStreamElementType;

@Path("/api/note")
@Tag(ref = "Note API")
@RequestScoped
public class NoteResource extends TmfApiBase<NoteEntity, NoteRepository> {

  public NoteResource(HttpHeaders headers, @Context UriInfo uriInfo, Request request) {
    init(headers, uriInfo, request);
  }

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "List all notes.",
      description =
          """
      * If you use the `application/json` media type, the response will be a paginated list of notes.
      * If you use the `text/event-stream` media type, the response will be a stream of notes.

        Note that the stream way does not support pagination. So the `limit` and `offset` query parameters will be ignored.

      **TIP**: You can set the query parameter `limit` to `0` to disable pagination and retrieve easily the number of notes.
      """)
  @APIResponse(
      responseCode = "200",
      headers = {@Header(ref = "X-Total-Count"), @Header(ref = "X-Result-Count")},
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(type = SchemaType.ARRAY, implementation = Note.class)),
      description =
          """
        **Note**:
        * The paginated way will return a `206 Partial Content` response code.
        * The output can contain extra fields that are not defined in the API.
        """)
  @Override
  public Uni<Response> find(@Valid @BeanParam PaginateQuery paginateQuery) {
    return super.find(paginateQuery);
  }

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @RestStreamElementType(MediaType.APPLICATION_JSON)
  @APIResponse(
      responseCode = "200",
      content =
          @Content(
              example = OpenAPI.DOC_EXAMPLE_STREAM,
              mediaType = MediaType.SERVER_SENT_EVENTS,
              schema = @Schema(implementation = Note.class)))
  @Override
  public Multi<Map<String, Object>> stream() {
    return super.stream();
  }

  @HEAD
  @Operation(summary = "Count the number of notes.")
  @Override
  public Uni<Response> count() {
    return super.count();
  }

  @POST
  @Operation(
      summary = "Create a new note.",
      description =
          """
      This endpoint is used to create a new note.

      It can accept any kind of note.
      So all extra fields will be stored in the database.
      And the output can contain extra fields that are not defined in the API.

      The created note will be assigned to the domain
      specified in the header `X-Client-Id`.
      """)
  @RequestBody(
      description = "The created note.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = Note.class)))
  @APIResponse(
      responseCode = "201",
      description = "The note has been created.",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = Note.class)))
  @Override
  public Uni<Response> create(Map<String, Object> troubleTicket) {
    return super.create(troubleTicket);
  }

  @GET
  @Path("{id}")
  @Operation(
      summary = "Get a note.",
      description =
          """
        Get a note by its ID.

        The output can contain extra fields that are not defined in the API.
        """)
  @APIResponse(
      responseCode = "200",
      description = "The note has been found.",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = Note.class)))
  @Override
  public Uni<Response> get(UUID id) {
    return super.get(id);
  }

  @DELETE
  @Path("{id}")
  @Operation(summary = "Delete a resource.")
  @Override
  public Uni<Response> delete(UUID id) {
    return super.delete(id);
  }
}
