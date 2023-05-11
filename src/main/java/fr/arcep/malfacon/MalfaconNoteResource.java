package fr.arcep.malfacon;

import fr.arcep.OpenAPI;
import fr.arcep.note.NoteService;
import fr.arcep.tmf.model.Note;
import fr.arcep.tmf.model.RelatedEntity;
import fr.arcep.troubleticket.TroubleTicketService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestStreamElementType;

@Path("api/malfacon/{malfaconId}/note")
@Tag(ref = "Malfacon Proxy")
public class MalfaconNoteResource {

  private static final String CLIENT_ID = "malfacon";

  @Inject @RestClient NoteService noteService;

  @Inject @RestClient TroubleTicketService troubleTicketService;

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List all notes")
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
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  @Parameter(ref = "filters")
  @Parameter(ref = "sort")
  @Parameter(ref = "limit")
  @Parameter(ref = "offset")
  @Parameter(ref = "fields")
  public Uni<Response> list(UUID malfaconId, @Schema(hidden = true) @Context UriInfo uriInfo) {
    return getTroubleTicket(malfaconId)
        .chain(() -> noteService.list(CLIENT_ID, getQueryParameters(malfaconId, uriInfo)))
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
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
              schema = @Schema(implementation = MalfaconAttachment.class)))
  public Multi<Object> stream(UUID malfaconId, @Schema(hidden = true) @Context UriInfo uriInfo) {
    return getTroubleTicket(malfaconId)
        .onItem()
        .transformToMulti(
            r -> noteService.stream(CLIENT_ID, getQueryParameters(malfaconId, uriInfo)))
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  @HEAD
  @Operation(summary = "Count the number of notes")
  @APIResponse(
      responseCode = "200",
      headers = {@Header(ref = "X-Total-Count")})
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  @Parameter(ref = "filters")
  public Uni<Response> count(UUID malfaconId, @Schema(hidden = true) @Context UriInfo uriInfo) {
    return getTroubleTicket(malfaconId)
        .chain(() -> noteService.count(CLIENT_ID, getQueryParameters(malfaconId, uriInfo)))
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  @POST
  @Operation(summary = "Create a new note.", description = OpenAPI.DOC_OPERATION_POST_DESC)
  @APIResponse(
      responseCode = "201",
      description = "The note has been created.",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = Note.class)))
  @APIResponse(ref = "error-400")
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  public Uni<Response> create(UUID malfaconId, @Valid Note note) {
    var relatedEntity = new RelatedEntity();
    relatedEntity.referredType = "malfaconTroubleTicket";
    relatedEntity.id = malfaconId.toString();
    relatedEntity.name = CLIENT_ID;

    note.relatedEntity = List.of(relatedEntity);

    return getTroubleTicket(malfaconId)
        .chain(() -> noteService.create(CLIENT_ID, note))
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  @GET
  @Path("{noteId}")
  @Operation(summary = "Get an note.")
  @APIResponse(
      responseCode = "200",
      description = "The note has been found.",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = Note.class)))
  @APIResponse(ref = "error-404")
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  public Uni<Response> get(UUID malfaconId, UUID noteId) {
    return getTroubleTicket(malfaconId)
        .chain(() -> noteService.get(CLIENT_ID, noteId))
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  @DELETE
  @Path("{noteId}")
  @Operation(summary = "Delete an note.")
  @APIResponse(responseCode = "204", description = "The note has been deleted.")
  @APIResponse(ref = "error-404")
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  public Uni<Response> delete(UUID malfaconId, UUID noteId) {
    return getTroubleTicket(malfaconId)
        .chain(() -> noteService.delete(CLIENT_ID, noteId))
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  private MultivaluedMap<String, String> getQueryParameters(UUID malfaconId, UriInfo uriInfo) {
    var queryParameters = new MultivaluedHashMap<String, String>(uriInfo.getQueryParameters());
    queryParameters.putSingle("relatedEntity.0.@referredType", "malfaconTroubleTicket");
    queryParameters.putSingle("relatedEntity.0.id", malfaconId.toString());
    return queryParameters;
  }

  private Uni<Response> getTroubleTicket(UUID malfaconId) {
    return troubleTicketService.get(CLIENT_ID, malfaconId);
  }
}
