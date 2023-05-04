package fr.arcep.tmf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.Length;

@Schema(
    description =
        """
    Based on the TM Forum Open API Data Model for EntityAttachment.

    For more information, see [TM Forum Open API DataModel](https://datamodel.tmforum.org/en/latest/Common/EntityAttachment/)
    and the [TM Forum Open API schema](https://raw.githubusercontent.com/tmforum-rand/schemas/candidates/Common/EntityAttachment.schema.json).
    """)
public class Attachment extends Base {

  public Attachment() {
    super();
    setBaseType("Attachment");
    setType("Attachment");
  }

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "The unique identifier of the attachment.", readOnly = true)
  public String id;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "The date on which the attachment was created.", readOnly = true)
  public Date creationDate;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "The date and time that the attachment was last updated.", readOnly = true)
  public Date lastUpdate;

  @Schema(description = "The name of the attachment.", example = "my_picture.jpg", required = true)
  @NotBlank
  @Length(min = 5, max = 255)
  public String name;

  @Schema(description = "Description of the attachment.", example = "My picture")
  public String description;

  @Schema(
      description = "Attachment type such as video, picture, document, etc.",
      example = "picture")
  public String attachmentType;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(
      description = "Attachment mime type such as extension file for video, picture and document.",
      example = "image/jpeg",
      readOnly = true)
  public String mimeType;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "The size of the attachment.", readOnly = true)
  public Long size;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(description = "The entity against which the attachment is associated.", readOnly = true)
  public List<RelatedEntity> relatedEntity = new ArrayList<>();

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(
      description = "The related party(ies) that are associated to the attachment.",
      readOnly = true)
  public List<RelatedParty> relatedParty = new ArrayList<>();
}
