package fr.arcep.attachment;

import fr.arcep.OpenAPI;
import fr.arcep.tmf.model.Attachment;
import fr.arcep.tmf.model.params.PaginateQuery;
import fr.arcep.tmf.util.TmfApiBase;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestStreamElementType;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Path("/api/attachment")
@Tag(ref = "Attachment API")
public class AttachmentResource extends TmfApiBase<AttachmentEntity, AttachmentRepository> {

  private final S3AsyncClient s3;

  public AttachmentResource(
      HttpHeaders headers, @Context UriInfo uriInfo, Request request, S3AsyncClient s3) {
    super(headers, uriInfo, request);
    this.s3 = s3;
  }

  private final SimpleDateFormat headerDateFormat =
      new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "List all attachments.",
      description =
          """
      * If you use the `application/json` media type, the response will be a paginated list of attachments.
      * If you use the `text/event-stream` media type, the response will be a stream of attachments.

        Note that the stream way does not support pagination. So the `limit` and `offset` query parameters will be ignored.

      **TIP**: You can set the query parameter `limit` to `0` to disable pagination and retrieve easily the number of attachments.
      """)
  @APIResponse(
      responseCode = "200",
      headers = {@Header(ref = "X-Total-Count"), @Header(ref = "X-Result-Count")},
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(type = SchemaType.ARRAY, implementation = Attachment.class)),
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
              schema = @Schema(implementation = Attachment.class)))
  @Override
  public Multi<Map<String, Object>> stream() {
    return super.stream();
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @RequestBody(content = @Content(schema = @Schema(implementation = AttachmentFormData.class)))
  @Operation(
      summary = "Create a new attachment.",
      description =
          """
      This endpoint is used to create a new attachment.

      It can accept any kind of attachment.
      So all extra fields will be stored in the database.
      And the output can contain extra fields that are not defined in the API.

      The created attachment will be assigned to the domain
      specified in the header `X-Client-Id`.
      """)
  @APIResponse(
      responseCode = "201",
      description = "The attachment has been created.",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = Attachment.class)))
  @Parameter(ref = "X-Allowed-MimeType")
  public Uni<Response> create(
      AttachmentFormData data, @HeaderParam("X-Allowed-MimeType") String allowedMimeType) {
    var filename = String.valueOf(data.attachment.get("name"));

    if (filename.isBlank()) {
      throw new BadRequestException("The filename is required.");
    }

    var mimeType = getMimeType(data.file, filename);
    var ext = mimeType.getExtension();

    checkMimeType(mimeType, allowedMimeType);

    if (!filename.endsWith(ext)) {
      filename += ext;
    }

    data.attachment.put("mimeType", mimeType.getName());
    data.attachment.put("size", data.file.length());
    data.attachment.put("name", filename);

    return repository
        .persistAndNotify(data.attachment, clientId)
        .chain(t -> putObject(data, t.id).replaceWith(t))
        .map(AttachmentEntity::toMap)
        .map(t -> Response.status(Response.Status.CREATED).entity(t).build());
  }

  @GET
  @Path("{id}")
  @Operation(
      summary = "Get an attachment.",
      description =
          """
        Get a attachment by its ID.

        The output can contain extra fields that are not defined in the API.
        """)
  @APIResponse(
      responseCode = "200",
      description = "The attachment has been found.",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = Attachment.class)))
  @Override
  public Uni<Response> get(UUID id) {
    return super.get(id);
  }

  @GET
  @Path("{id}/content")
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
  @Parameter(ref = "X-Client-Id")
  public Uni<Response> download(UUID id) {
    return getEntityById(id).chain(this::getObject);
  }

  private Uni<Void> getBucket(String domain) {
    var bucketRequest = CreateBucketRequest.builder().bucket(domain).build();

    return Uni.createFrom()
        .completionStage(() -> s3.createBucket(bucketRequest))
        .onFailure(BucketAlreadyExistsException.class)
        .recoverWithNull()
        .onFailure()
        .invoke(t -> Log.fatal("Bucket " + domain + " does not exists", t))
        .replaceWithVoid();
  }

  private Uni<Void> putObject(AttachmentFormData data, String id) {
    var putRequest = PutObjectRequest.builder().bucket(clientId).key(id).build();

    return getBucket(clientId)
        .chain(
            () ->
                Uni.createFrom()
                    .completionStage(
                        () -> s3.putObject(putRequest, AsyncRequestBody.fromFile(data.file))))
        .onFailure()
        .invoke(t -> Log.error("Attachment " + id + " not created", t))
        .replaceWithVoid();
  }

  private Uni<Response> getObject(AttachmentEntity entity) {
    ResponseBuilder response = Response.status(Response.Status.OK);

    var requestBuilder = GetObjectRequest.builder().bucket(clientId).key(entity.id);

    var ifMatch = headers.getHeaderString("If-Match");
    if (ifMatch != null && !ifMatch.isBlank()) {
      requestBuilder.ifMatch(ifMatch);
    }

    var ifNoneMatch = headers.getHeaderString("If-None-Match");
    if (ifNoneMatch != null && !ifNoneMatch.isBlank()) {
      requestBuilder.ifNoneMatch(ifNoneMatch);
    }

    var ifModifiedSince = headers.getHeaderString("If-Modified-Since");
    if (ifModifiedSince != null && !ifModifiedSince.isBlank()) {
      try {
        var d = headerDateFormat.parse(ifModifiedSince);
        requestBuilder.ifModifiedSince(d.toInstant());
      } catch (ParseException e) {
        throw new BadRequestException("Invalid date format for If-Modified-Since");
      }
    }

    var ifUnmodifiedSince = headers.getHeaderString("If-Unmodified-Since");
    if (ifUnmodifiedSince != null && !ifUnmodifiedSince.isBlank()) {
      try {
        var d = headerDateFormat.parse(ifUnmodifiedSince);
        requestBuilder.ifUnmodifiedSince(d.toInstant());
      } catch (ParseException e) {
        throw new BadRequestException("Invalid date format for If-Unmodified-Since");
      }
    }

    return Uni.createFrom()
        .completionStage(
            () -> s3.getObject(requestBuilder.build(), AsyncResponseTransformer.toBytes()))
        .onFailure()
        .invoke(t -> Log.error("Attachment " + entity.id + " not found", t))
        .map(
            o ->
                response
                    .entity(o.asByteArray())
                    .header("Content-Disposition", "attachment; filename=\"" + entity.name + "\"")
                    .header("Content-Type", entity.payload.get("mimeType"))
                    .header("Last-Modified", o.response().lastModified().toString())
                    .header("ETag", o.response().eTag())
                    .header("Content-Length", String.valueOf(o.response().contentLength()))
                    .build());
  }

  private MimeType getMimeType(File file, String name) {
    try (var fileStream = new FileInputStream(file)) {
      var type = new Tika().detect(fileStream, name);
      MimeTypes.getDefaultMimeTypes().forName(type).getName();
      return MimeTypes.getDefaultMimeTypes().forName(type);
    } catch (Exception e) {
      Log.error("Unable to find mimetype", e);
      throw new BadRequestException("Unable to find mimetype");
    }
  }

  private void checkMimeType(MimeType mimeType, String allowedMimeTypes) {
    if (allowedMimeTypes == null || allowedMimeTypes.isBlank()) {
      return;
    }

    for (var pattern : getPatterns(allowedMimeTypes)) {
      if (pattern.matcher(mimeType.getName()).matches()) {
        return;
      }
    }

    throw new BadRequestException("Invalid mimetype. Allowed: " + allowedMimeTypes);
  }

  private List<Pattern> getPatterns(String allowedMimeTypes) {
    var patterns = new ArrayList<Pattern>();
    for (var pattern : allowedMimeTypes.split("\\s*;\\s*")) {
      patterns.add(Pattern.compile(pattern));
    }

    return patterns;
  }
}
