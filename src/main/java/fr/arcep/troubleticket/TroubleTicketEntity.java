package fr.arcep.troubleticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.arcep.tmf.model.StatusChange;
import fr.arcep.tmf.util.EntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hibernate.validator.constraints.Length;

@MongoEntity(collection = "troubleticket")
public class TroubleTicketEntity extends EntityBase {

  private static final List<String> FIELDS_IGNORED =
      List.of("id", "creationDate", "lastUpdate", "resolutionDate", "closed", "statusChange");

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public Date resolutionDate;

  @JsonProperty public Date expectedResolutionDate;

  @NotBlank
  @Length(min = 5)
  @JsonProperty
  public String name;

  @JsonProperty public String description;
  @JsonProperty public String priority;
  @JsonProperty public String severity;
  @JsonProperty public String externalId;
  @JsonProperty public String status;
  @JsonProperty public Date statusChangeDate;
  @JsonProperty public String statusChangeReason;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public boolean closed;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public List<StatusChange> statusChange = new ArrayList<>();

  public void setClose() {
    this.closed = this.resolutionDate != null;
  }

  public static TroubleTicketEntity fromMap(Map<String, Object> map) {
    return EntityBase.fromMap(map, TroubleTicketEntity.class, FIELDS_IGNORED);
  }
}
