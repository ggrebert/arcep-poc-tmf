package fr.arcep.note;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.arcep.tmf.util.EntityBase;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hibernate.validator.constraints.Length;

public class NoteEntity extends EntityBase {

  private static final List<String> FIELDS_IGNORED = List.of("id", "date");

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public String author;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY, value = "date")
  @SuppressWarnings("java:S2387")
  public Date creationDate = new Date();

  @JsonIgnore
  @SuppressWarnings("java:S2387")
  public Date lastUpdate;

  @JsonProperty
  @NotBlank
  @Length(min = 5)
  public String text;

  public static NoteEntity fromMap(Map<String, Object> map) {
    return EntityBase.fromMap(map, NoteEntity.class, FIELDS_IGNORED);
  }
}
