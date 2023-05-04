package fr.arcep.attachment;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.arcep.tmf.model.StatusChange;
import fr.arcep.tmf.util.EntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@MongoEntity(collection = "attachment")
public class AttachmentEntity extends EntityBase {

  private static final List<String> FIELDS_IGNORED =
      List.of("id", "creationDate", "lastUpdate");

  @JsonProperty public long size;
  @JsonProperty public String name;
  @JsonProperty public String description;
  @JsonProperty public String mimeType;
  @JsonProperty public String status;
  @JsonProperty public Date statusChangeDate;
  @JsonProperty public String statusChangeReason;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public List<StatusChange> statusChange = new ArrayList<>();

  public static AttachmentEntity fromMap(Map<String, Object> map) {
    return EntityBase.fromMap(map, AttachmentEntity.class, FIELDS_IGNORED);
  }
}
