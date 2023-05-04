package fr.arcep.malfacon;

import fr.arcep.OpenAPI;
import fr.arcep.attachment.AttachmentService;
import fr.arcep.tmf.model.RelatedEntity;
import fr.arcep.troubleticket.TroubleTicketService;
import io.quarkus.logging.Log;
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
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestStreamElementType;

@Path("api/malfacon/{malfaconId}/attachment")
@Tag(ref = "Malfacon Proxy")
public class MalfaconAttachmentResource {

  private static final String CLIENT_ID = "malfacon";

  @Inject @RestClient AttachmentService attachmentService;

  @Inject @RestClient TroubleTicketService troubleTicketService;

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List all attachments of type `MalfaconAttachment`")
  @APIResponse(
      responseCode = "200",
      headers = {@Header(ref = "X-Total-Count"), @Header(ref = "X-Result-Count")},
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(type = SchemaType.ARRAY, implementation = MalfaconAttachment.class)),
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
        .chain(() -> attachmentService.list(CLIENT_ID, getQueryParameters(malfaconId, uriInfo)))
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
  public Multi<Response> stream(UUID malfaconId, @Schema(hidden = true) @Context UriInfo uriInfo) {
    return getTroubleTicket(malfaconId)
        .onItem()
        .transformToMulti(
            r -> attachmentService.stream(CLIENT_ID, getQueryParameters(malfaconId, uriInfo)))
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  @HEAD
  @Operation(summary = "Count the number of Attachment entities")
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
        .chain(() -> attachmentService.count(CLIENT_ID, getQueryParameters(malfaconId, uriInfo)))
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Operation(summary = "Create a new attachment.")
  @RequestBody(
      content = @Content(schema = @Schema(implementation = MalfaconAttachmentFormData.class)))
  @APIResponse(
      responseCode = "201",
      description = "The attachment has been created.",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = MalfaconAttachment.class)))
  @APIResponse(ref = "error-400")
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  public Uni<Response> createAttachment(UUID malfaconId, @Valid MalfaconAttachmentFormData data) {
    var relatedEntity = new RelatedEntity();
    relatedEntity.referredType = "malfaconTroubleTicket";
    relatedEntity.id = malfaconId.toString();
    relatedEntity.name = CLIENT_ID;

    data.attachment.relatedEntity = List.of(relatedEntity);

    return getTroubleTicket(malfaconId)
        .chain(
            () ->
                attachmentService.create(
                    CLIENT_ID, data.toAttachmentFormData(), "image/.+;application/pdf"))
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  @GET
  @Path("{attachmentId}")
  @Operation(summary = "Get an attachment.")
  @APIResponse(
      responseCode = "200",
      description = "The attachment has been found.",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = MalfaconAttachment.class)))
  @APIResponse(ref = "error-404")
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  public Uni<Response> getAttachment(UUID malfaconId, UUID attachmentId) {
    return getTroubleTicket(malfaconId)
        .chain(() -> attachmentService.get(CLIENT_ID, attachmentId))
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  @GET
  @Path("{attachmentId}/content")
  @Operation(summary = "Download the attachment content.")
  @Parameter(ref = "Range")
  @Parameter(ref = "If-Match")
  @Parameter(ref = "If-None-Match")
  @Parameter(ref = "If-Modified-Since")
  @Parameter(ref = "If-Unmodified-Since")
  @APIResponse(
      responseCode = "200",
      description = "The attachment.",
      content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM))
  @APIResponse(
      responseCode = "206",
      description = "Bytes range of the attachment.",
      content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM))
  @APIResponse(ref = "error-404")
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  public Uni<Response> download(UUID malfaconId, UUID attachmentId) {
    return getTroubleTicket(malfaconId)
        .chain(() -> attachmentService.download(CLIENT_ID, attachmentId))
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  @DELETE
  @Path("{attachmentId}")
  @Operation(summary = "Delete an attachment.")
  @APIResponse(responseCode = "204", description = "The attachment has been deleted.")
  @APIResponse(ref = "error-404")
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  public Uni<Response> delete(UUID malfaconId, UUID attachmentId) {
    return getTroubleTicket(malfaconId)
        .chain(() -> attachmentService.delete(CLIENT_ID, attachmentId))
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }

  private MultivaluedMap<String, String> getQueryParameters(UUID malfaconId, UriInfo uriInfo) {
    var queryParameters = new MultivaluedHashMap<String, String>(uriInfo.getQueryParameters());
    queryParameters.putSingle("relatedEntity.referredType", "malfaconTroubleTicket");
    queryParameters.putSingle("relatedEntity.id", malfaconId.toString());
    return queryParameters;
  }

  private Uni<Response> getTroubleTicket(UUID malfaconId) {
    return troubleTicketService.get(CLIENT_ID, malfaconId);
  }
}
