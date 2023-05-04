package fr.arcep.tmf.model.params;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.Range;

@Schema(hidden = true)
public class PaginateQuery {

  @QueryParam("offset")
  @DefaultValue("0")
  public int offset;

  @QueryParam("limit")
  @DefaultValue("10")
  @Range(min = 0, max = 100)
  public int limit;
}
