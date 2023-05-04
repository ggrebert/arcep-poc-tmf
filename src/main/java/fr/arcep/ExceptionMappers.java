package fr.arcep;

import fr.arcep.tmf.model.Error;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse.Status;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

  @ServerExceptionMapper
  public Uni<Response> handleException(Exception e) {
    var error = new Error();
    error.code = "500";
    error.message = "Internal Server Error";
    error.reason = "unexpected error";

    Log.fatal(e.getLocalizedMessage(), e);

    return Uni.createFrom()
        .item(Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build());
  }

  @ServerExceptionMapper
  public Uni<Response> handleException(WebApplicationException e) {
    var error = new Error();
    error.code = e.getResponse().getStatus() + "";
    error.message = e.getResponse().getStatusInfo().getReasonPhrase();
    error.reason = e.getLocalizedMessage();

    return Uni.createFrom()
        .item(Response.status(e.getResponse().getStatus()).entity(error).build());
  }

  @ServerExceptionMapper
  public Uni<Response> handleException(ConstraintViolationException e) {
    var error = new Error();
    error.code = "400";
    error.message = "Bad Request";
    error.reason = e.getLocalizedMessage();

    return Uni.createFrom().item(Response.status(400).entity(error).build());
  }

  @ServerExceptionMapper
  public Uni<Response> handleException(NotFoundException e) {
    var error = new Error();
    error.code = "404";
    error.message = "Not Found";
    error.reason = e.getLocalizedMessage();

    return Uni.createFrom().item(Response.status(404).entity(error).build());
  }
}
