package fr.arcep.tmf.model;

import java.util.Date;
import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    description = "The status of an entity and the reason for the status change.",
    name = "StatusChange",
    title = "Status Change",
    externalDocs =
        @ExternalDocumentation(url = "https://datamodel.tmforum.org/en/latest/Common/StatusChange"))
public class StatusChange extends Base {

  public StatusChange() {
    super();
    setBaseType("StatusChange");
    setSchemaLocation(
        "https://raw.githubusercontent.com/tmforum-rand/schemas/candidates/Common/StatusChange.schema.json");
  }

  public StatusChange(String status) {
    this.status = status;
  }

  public StatusChange(String status, Date changeDate) {
    this(status);
    this.changeDate = changeDate;
  }

  public StatusChange(String status, Date changeDate, String changeReason) {
    this(status, changeDate);
    this.changeReason = changeReason;
  }

  public StatusChange(String status, Date changeDate, String changeReason, String author) {
    this(status, changeDate, changeReason);
    this.author = author;
  }

  @Schema(description = "The status of the entity.", example = "Active", readOnly = true)
  public String status;

  @Schema(description = "The reason for the status change.", example = "Created", readOnly = true)
  public String changeReason;

  @Schema(
      description = "The date the status was changed.",
      example = "2019-01-01T00:00:00Z",
      readOnly = true)
  public Date changeDate;

  @Schema(description = "The author of the status change.", example = "John Doe", readOnly = true)
  public String author;
}
