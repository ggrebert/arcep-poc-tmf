package fr.arcep.tmf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@RegisterForReflection
public class Base {

  protected String baseType = null;
  protected String type = null;
  protected String schemaLocation = null;

  @JsonProperty(value = "@baseType", access = JsonProperty.Access.READ_ONLY)
  @JsonInclude(content = JsonInclude.Include.NON_NULL)
  @Schema(
      description = "When sub-classing, this defines the super-class",
      example = "myBaseType",
      readOnly = true)
  public String getBaseType() {
    return baseType;
  }

  public Base setBaseType(String baseType) {
    this.baseType = baseType;
    return this;
  }

  @JsonProperty(value = "@type", access = JsonProperty.Access.READ_ONLY)
  @JsonInclude(content = JsonInclude.Include.NON_NULL)
  @Schema(
      description = "When sub-classing, this defines the sub-class entity name",
      example = "MyCustomType",
      readOnly = true)
  public String getType() {
    return type;
  }

  public Base setType(String type) {
    this.type = type;
    return this;
  }

  @JsonProperty(value = "@schemaLocation", access = JsonProperty.Access.READ_ONLY)
  @JsonInclude(content = JsonInclude.Include.NON_NULL)
  @Schema(
      description =
          "A URI to a JSON-Schema file that defines additional attributes and relationships",
      readOnly = true)
  public String getSchemaLocation() {
    return schemaLocation;
  }

  public Base setSchemaLocation(String schemaLocation) {
    this.schemaLocation = schemaLocation;
    return this;
  }
}
