package fr.arcep.troubleticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class TroubleTicketNotification {

  public String id;
  public Date creationDate;
  public Date lastUpdate;
  public String priority;
  public String severity;
  public String status;
  public String statusChangeReason;
  public Date statusChangeDate;
  public String domain;

  @JsonProperty(value = "@type")
  public String type;

  public TroubleTicketNotification() {}

  public TroubleTicketNotification(TroubleTicketEntity entity) {
    this.id = entity.id;
    this.creationDate = entity.creationDate;
    this.lastUpdate = entity.lastUpdate;
    this.priority = entity.priority;
    this.severity = entity.severity;
    this.status = entity.status;
    this.statusChangeReason = entity.statusChangeReason;
    this.statusChangeDate = entity.statusChangeDate;
    this.domain = entity.domain;
    this.type = entity.payload.getString("@type");
  }
}
