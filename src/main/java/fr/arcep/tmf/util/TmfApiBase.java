package fr.arcep.tmf.util;

import fr.arcep.tmf.model.params.PaginateQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bson.Document;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.RestStreamElementType;

public abstract class TmfApiBase<E extends EntityBase, T extends RepositoryBase<E>> {

  @Inject protected TMFilter tmFilter;

  @Inject protected T repository;

  protected HttpHeaders headers;
  protected Request request;
  protected UriInfo uriInfo;
  protected String clientId;

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  @Parameter(ref = "filters")
  @Parameter(ref = "sort")
  @Parameter(ref = "limit")
  @Parameter(ref = "offset")
  @Parameter(ref = "fields")
  @Parameter(ref = "X-Client-Id")
  public Uni<Response> find(@Valid PaginateQuery paginateQuery) {
    var response = Response.status(Response.Status.PARTIAL_CONTENT);
    var queryFilter = tmFilter.process(getQuery(), uriInfo.getQueryParameters());
    var sort = tmFilter.getSort(uriInfo);
    var query =
        sort.isPresent()
            ? repository.find(queryFilter.toJson(), sort.get())
            : repository.find(queryFilter);

    Multi<E> queryData =
        paginateQuery.limit == 0
            ? Multi.createFrom().empty()
            : query.range(paginateQuery.offset, paginateQuery.limit - 1).stream();

    return Uni.combine()
        .all()
        .unis(
            query.count(),
            queryData.map(E::toMap).map(t -> tmFilter.filterFields(t, uriInfo)).collect().asList())
        .combinedWith(
            (count, list) ->
                response
                    .header("X-Total-Count", count)
                    .header("X-Result-Count", list.size())
                    .entity(list)
                    .build());
  }

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @RestStreamElementType(MediaType.APPLICATION_JSON)
  public Multi<Map<String, Object>> stream() {
    var queryFilter = tmFilter.process(getQuery(), uriInfo.getQueryParameters());
    var sort = tmFilter.getSort(uriInfo);
    var query =
        sort.isPresent()
            ? repository.find(queryFilter.toJson(), sort.get())
            : repository.find(queryFilter);

    return query.stream().map(E::toMap).map(t -> tmFilter.filterFields(t, uriInfo));
  }

  @APIResponse(
      responseCode = "200",
      headers = {@Header(ref = "X-Total-Count")})
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  @Parameter(ref = "filters")
  @Parameter(ref = "X-Client-Id")
  public Uni<Response> count() {
    return Uni.createFrom()
        .item(tmFilter.process(getQuery(), uriInfo.getQueryParameters()))
        .chain(q -> repository.find(q).count())
        .map(count -> Response.noContent().header("X-Total-Count", count).build());
  }

  @APIResponse(ref = "error-404")
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  @Parameter(ref = "X-Client-Id")
  public Uni<Response> get(UUID id) {
    return getEntityById(id).map(E::toMap).map(t -> Response.ok(t).build());
  }

  @APIResponse(responseCode = "204", description = "The resource has been deleted.")
  @APIResponse(ref = "error-404")
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  @Parameter(ref = "X-Client-Id")
  public Uni<Response> delete(UUID id) {
    return getEntityById(id)
        .chain(repository::deleteAndNotify)
        .replaceWith(Response.noContent().build());
  }

  @APIResponse(ref = "error-400")
  @APIResponse(ref = "error-500")
  @APIResponse(ref = "error-501")
  @APIResponse(ref = "error-502")
  @APIResponse(ref = "error-503")
  @Parameter(ref = "X-Client-Id")
  public Uni<Response> create(Map<String, Object> data) {
    return repository
        .persistAndNotify(data, clientId)
        .map(E::toMap)
        .map(t -> Response.status(Response.Status.CREATED).entity(t).build());
  }

  protected List<String> getAdminClients() {
    return List.of("admin");
  }

  protected String getDomainField() {
    return "domain";
  }

  protected Uni<E> getEntityById(UUID id) {
    return repository.find(getQuery(id)).stream()
        .collect()
        .first()
        .onItem()
        .ifNull()
        .failWith(() -> new NotFoundException("Ressource not found"));
  }

  protected Document getQuery() {
    return getAdminClients().contains(clientId)
        ? new Document()
        : Document.parse(String.format("{'%s': '%s'}", getDomainField(), clientId));
  }

  protected Document getQuery(String id) {
    return getAdminClients().contains(clientId) && request.getMethod().equals("GET")
        ? new Document("_id", id)
        : Document.parse(
            String.format("{'%s': '%s', '_id': '%s'}", getDomainField(), clientId, id));
  }

  protected Document getQuery(UUID id) {
    return getQuery(id.toString());
  }

  protected void init(HttpHeaders headers, UriInfo uriInfo, Request request) {
    this.request = request;
    this.uriInfo = uriInfo;
    this.headers = headers;

    clientId = headers.getHeaderString("X-Client-Id");
    if (clientId == null || clientId.isBlank()) {
      throw new UnauthorizedException("You are not authorized to access this resource");
    }
  }

  public static class UnauthorizedException extends WebApplicationException {
    public UnauthorizedException(String message) {
      super(message, Response.Status.UNAUTHORIZED);
    }
  }
}
