package fr.arcep.malfacon;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.arcep.tmf.model.Attachment;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.UniqueElements;

public class MalfaconAttachment extends Attachment {

  public MalfaconAttachment() {
    super();
    setType("MalfaconAttachment");
  }

  @UniqueElements public List<String> tags = new ArrayList<>();

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Schema(
      description = "The MIME type of the attachment",
      readOnly = true,
      example = "application/pdf")
  @SuppressWarnings("java:S2387")
  public String mimeType;
}
