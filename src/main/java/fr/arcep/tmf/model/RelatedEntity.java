package fr.arcep.tmf.model;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    externalDocs =
        @ExternalDocumentation(
            url = "https://datamodel.tmforum.org/en/latest/Common/RelatedEntity/"),
    description = "A reference to an entity, where the type of the entity is not known in advance.")
public class RelatedEntity extends EntityRef {

  public RelatedEntity() {
    super();
    setType("RelatedEntity");
    setBaseType("RelatedEntity");
  }

  @Schema(description = "Role of the related entity.")
  @NotBlank
  public String role;
}
