package fr.arcep.tmf.model;

import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    externalDocs =
        @ExternalDocumentation(
            url = "https://datamodel.tmforum.org/en/latest/EngagedParty/RelatedParty/"),
    description =
        """
        Related Entity reference. A related party defines party or party role linked to a specific entity.
        """)
public class RelatedParty extends RelatedEntity {

  public RelatedParty() {
    super();
    setBaseType("RelatedParty");
  }
}
