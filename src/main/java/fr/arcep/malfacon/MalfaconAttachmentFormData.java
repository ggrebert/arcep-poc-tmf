package fr.arcep.malfacon;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arcep.attachment.AttachmentFormData;
import io.quarkus.arc.Arc;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

public class MalfaconAttachmentFormData {

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
  @Schema(implementation = MalfaconAttachment.class, required = true)
  public MalfaconAttachment attachment;

  public AttachmentFormData toAttachmentFormData() {
    var om = Arc.container().instance(ObjectMapper.class).get();

    var formData = new AttachmentFormData();
    formData.file = file;
    formData.attachment = om.convertValue(attachment, new TypeReference<Map<String, Object>>() {});

    return formData;
  }
}
