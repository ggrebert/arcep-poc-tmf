package fr.arcep.eintervention;

import fr.arcep.eintervention.model.InterventionDO;
import fr.arcep.eintervention.model.TroubleTicketIntervention;
import fr.arcep.troubleticket.TroubleTicketService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@Path("/api/e-intervention")
@Tag(ref = "E-Intervention Proxy")
public class InterventionResource {

  private static final String CLIENT_ID = "e-intervention";

  @Inject @RestClient TroubleTicketService troubleTicketService;

  @POST
  @Path("intervention_DO")
  @Operation(
      summary = "Flux de déclaration d’intervention du DO vers l’OI.",
      description =
          "Opération permettant au DO de créer et mettre à jour une intervention chez l’OI.")
  @APIResponse(
      responseCode = "200",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = InterventionDO.Response.class)))
  public Uni<Response> interventionDO(@Valid InterventionDO intervention) {
    MultivaluedMap<String, String> q = new MultivaluedHashMap<>();
    q.putSingle("externalId", intervention.refDO);
    q.putSingle("name", "InterventionDO");

    return troubleTicketService
        .list(CLIENT_ID, q)
        .map(r -> r.readEntity(new GenericType<List<TroubleTicketIntervention>>() {}))
        .map(l -> l.isEmpty() ? new TroubleTicketIntervention(intervention) : l.get(0))
        .chain(
            t -> {
              if (t.id == null || t.id.isBlank()) {
                return troubleTicketService
                    .create(CLIENT_ID, t)
                    .map(r -> r.readEntity(TroubleTicketIntervention.class));
              }

              // TODO: update
              return Uni.createFrom().item(t);
            })
        .map(TroubleTicketIntervention::toResponse)
        .map(r -> Response.ok(r).build())
        .onFailure(WebApplicationException.class)
        .recoverWithItem(t -> WebApplicationException.class.cast(t).getResponse());
  }
}
