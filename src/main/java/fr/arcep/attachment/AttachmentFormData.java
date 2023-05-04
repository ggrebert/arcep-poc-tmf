package fr.arcep.attachment;

import fr.arcep.tmf.model.Attachment;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

public class AttachmentFormData {

  @RestForm("file")
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  @Schema(
      format = "binary",
      type = SchemaType.STRING,
      description = "The file to upload",
      implementation = String.class,
      required = true)
  public File file;

  @RestForm("attachment")
  @PartType(MediaType.APPLICATION_JSON)
  @Schema(implementation = Attachment.class, required = true)
  public Map<String, Object> attachment;
}
