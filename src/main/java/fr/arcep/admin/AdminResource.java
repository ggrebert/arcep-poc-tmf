package fr.arcep.admin;

import fr.arcep.OpenAPI;
import fr.arcep.tmf.model.TroubleTicket;
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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
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

@Path("/api/admin")
@Tag(ref = "Admin Proxy")
public class AdminResource {

  private static final String CLIENT_ID = "admin";

  @Inject @RestClient TroubleTicketService troubleTicketService;

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List all trouble tickets.", description = OpenAPI.DOC_OPERATION_LIST_DESC)
  @APIResponse(
      responseCode = "200",
      headers = {@Header(ref = "X-Total-Count"), @Header(ref = "X-Result-Count")},
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(type = SchemaType.ARRAY, implementation = TroubleTicket.class)),
      description =
          """
        Note that the paginated way will return a `206 Partial Content` response code.
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
  public Uni<Response> list(@Schema(hidden = true) @Context UriInfo uriInfo) {
    return troubleTicketService
        .list(CLIENT_ID, uriInfo.getQueryParameters())
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
              schema = @Schema(implementation = TroubleTicket.class)))
  public Multi<Object> stream(@Schema(hidden = true) @Context UriInfo uriInfo) {
    return troubleTicketService.stream(CLIENT_ID, uriInfo.getQueryParameters())
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  @HEAD
  @Operation(summary = "Count the number of trouble tickets.")
  @APIResponse(
      responseCode = "200",
      headers = {@Header(ref = "X-Total-Count")})
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  @Parameter(ref = "filters")
  public Uni<Response> count(@Schema(hidden = true) @Context UriInfo uriInfo) {
    return troubleTicketService
        .count(CLIENT_ID, uriInfo.getQueryParameters())
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  @POST
  @Operation(
      summary = "Create a new trouble ticket of type 'admin'.",
      description = OpenAPI.DOC_OPERATION_POST_DESC)
  @APIResponse(
      responseCode = "201",
      description = "The trouble ticket has been created.",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = Admin.class)))
  @APIResponse(ref = "error-400")
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  public Uni<Response> create(@Valid Admin malfacon) {
    return troubleTicketService
        .create(CLIENT_ID, malfacon)
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "Get a trouble ticket.", description = OpenAPI.DOC_OPERATION_GET_DESC)
  @APIResponse(
      responseCode = "200",
      description = "The trouble ticket has been found.",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = TroubleTicket.class)))
  @APIResponse(ref = "error-404")
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  public Uni<Response> get(UUID id) {
    return troubleTicketService
        .get(CLIENT_ID, id)
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  @DELETE
  @Path("/{id}")
  @Operation(
      summary = "Delete a trouble ticket of type 'admin'.",
      description = OpenAPI.DOC_OPERATION_DELETE_DESC)
  @APIResponse(responseCode = "204", description = "The trouble ticket has been deleted.")
  @APIResponse(ref = "error-404")
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  public Uni<Response> delete(UUID id) {
    return troubleTicketService
        .delete(CLIENT_ID, id)
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }
}
