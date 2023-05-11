package fr.arcep.tmf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class Note extends Base {

  public Note() {
    super();
    setBaseType("Note");
    setType("Note");
    setSchemaLocation(
        "https://raw.githubusercontent.com/tmforum-rand/schemas/candidates/Common/Note.schema.json");
  }

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "The unique identifier of the note.", readOnly = true)
  public UUID id;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "Author of the note.", readOnly = true)
  public String author;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "The date the note was created.", readOnly = true)
  public Date date;

  @Schema(description = "The text of the note.", example = "My comment", required = true)
  public String text;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "The entity against which the attachment is associated.", readOnly = true)
  public List<RelatedEntity> relatedEntity = new ArrayList<>();
}
