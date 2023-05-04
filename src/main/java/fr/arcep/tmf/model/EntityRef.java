package fr.arcep.tmf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    description = "A reference to an entity, where the type of the entity is not known in advance.",
    externalDocs =
        @ExternalDocumentation(url = "https://datamodel.tmforum.org/en/latest/Common/EntityRef"))
public class EntityRef extends Base {

  public EntityRef() {
    super();
    setBaseType("EntityRef");
  }

  @NotBlank
  @Schema(description = "The unique identifier of the entity.", required = true)
  public String id;

  @Schema(description = "Reference of the related entity.", readOnly = true)
  public String href;

  @Schema(description = "Name of the related entity.")
  public String name;

  @Schema(description = "Type of the related entity.")
  @JsonProperty(value = "@referredType")
  @NotBlank
  public String referredType;
}
