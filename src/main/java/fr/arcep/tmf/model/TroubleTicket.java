package fr.arcep.tmf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class TroubleTicket extends Base {

  public TroubleTicket() {
    super();
    setBaseType("TroubleTicket");
  }

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "The unique identifier of the trouble ticket.", readOnly = true)
  public String id = UUID.randomUUID().toString();

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(
      description = "The date on which the trouble ticket was created.",
      readOnly = true,
      example = "2021-01-01T00:00:00Z")
  public Date creationDate = new Date();

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(
      description = "The date and time that the trouble ticked was last updated.",
      readOnly = true,
      example = "2021-01-01T00:00:00Z")
  public Date lastUpdate;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(
      description = "The date and time that the trouble ticket was closed.",
      readOnly = true,
      example = "2021-01-01T00:00:00Z")
  public Date resolutionDate;

  @Schema(
      description = "The expected resolution date determined by the trouble ticket system.",
      example = "2021-01-01")
  public Date expectedResolutionDate;

  @Schema(
      description =
          """
        Name of the trouble ticket,
        typically a short description provided by the user that create the ticket.
        """,
      example = "My new ticket",
      required = true)
  public String name;

  @Schema(description = "Description of the trouble or issue.")
  public String description;

  @Schema(
      description =
          """
        The priority of the trouble ticket and how quickly the issue should be resolved.

        Example: Critical, High, Medium, Low.

        The value is set by the ticket management system considering the severity, ticket type etc...
        """)
  public String priority;

  @Schema(
      description =
          """
        The severity of the issue.
        Indicate the implication of the issue on the expected functionality e.g. of a system, application, service etc..

        Severity values can be for example : Critical, Major, Minor
        """)
  public String severity;

  @Schema(
      readOnly = true,
      description =
          """
        Additional identifier coming from an external system.

        This should be used to link the trouble ticket to an external system with the Hub API.
        """)
  public String externalId;

  @Schema(description = "Status of the trouble ticket.", example = "CREATED")
  public String status;

  @Schema(
      description = "The reason of the last status change.",
      example = "Creation of the ticket.")
  public String statusChangeReason;

  @Schema(description = "The related party(ies) that are associated to the ticket.")
  public List<RelatedParty> relatedParty = new ArrayList<>();

  @Schema(
      description =
          """
        An entity that is related to the ticket such as a bill, a product, etc.
        The entity against which the ticket is associated.
        """)
  public List<RelatedEntity> relatedEntity = new ArrayList<>();

  @JsonProperty(value = "closed", access = JsonProperty.Access.READ_ONLY)
  @Schema(
      description = "Indicate if the trouble ticket is closed or not.",
      readOnly = true,
      example = "false")
  public boolean closed;

  @Schema(readOnly = true, description = "The history of the status change of the ticket.")
  public List<StatusChange> statusChange = new ArrayList<>();

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "The date of the last status change.", readOnly = true)
  public Date getStatusChangeDate() {
    if (statusChange.isEmpty()) {
      return null;
    }

    return statusChange.get(statusChange.size() - 1).changeDate;
  }
}
