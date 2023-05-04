package fr.arcep.attachment;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestStreamElementType;

@RegisterRestClient(configKey = "attachmentService")
@Path("/api/attachment")
public interface AttachmentService {

  @GET
  Uni<Response> list(
      @HeaderParam("X-Client-Id") String clientId, @RestQuery MultivaluedMap<String, String> query);

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @RestStreamElementType(MediaType.APPLICATION_JSON)
  Multi<Response> stream(
      @HeaderParam("X-Client-Id") String clientId, @RestQuery MultivaluedMap<String, String> query);

  @HEAD
  Uni<Response> count(
      @HeaderParam("X-Client-Id") String clientId, @RestQuery MultivaluedMap<String, String> query);

  @GET
  @Path("{id}")
  Uni<Response> get(@HeaderParam("X-Client-Id") String clientId, @PathParam("id") UUID id);

  @GET
  @Path("{id}/content")
  Uni<Response> download(@HeaderParam("X-Client-Id") String clientId, @PathParam("id") UUID id);

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  Uni<Response> create(@HeaderParam("X-Client-Id") String clientId, AttachmentFormData data);

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  Uni<Response> create(
      @HeaderParam("X-Client-Id") String clientId,
      AttachmentFormData data,
      @HeaderParam("X-Allowed-MimeType") String allowedMimeType);

  @DELETE
  Uni<Response> delete(@HeaderParam("X-Client-Id") String clientId, @PathParam("id") UUID id);
}
