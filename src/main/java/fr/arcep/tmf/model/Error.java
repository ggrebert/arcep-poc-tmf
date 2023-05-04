package fr.arcep.tmf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "Error",
    description =
        """
    Used when an API throws an Error, typically with a HTTP error response-code (3xx, 4xx, 5xx)

    For more information, see [TM Forum Open API DataModel](https://datamodel.tmforum.org/en/latest/Common/Error/)
    and the [TM Forum Open API schema](https://raw.githubusercontent.com/tmforum-rand/schemas/candidates/Common/Error.schema.json).
    """)
@JsonInclude(content = JsonInclude.Include.NON_NULL)
@RegisterForReflection
public class Error extends Base {

  public Error() {
    setBaseType("Error");
    setSchemaLocation(
        "https://raw.githubusercontent.com/tmforum-rand/schemas/candidates/Common/Error.schema.json");
  }

  @JsonProperty(value = "code")
  @JsonInclude(content = JsonInclude.Include.NON_NULL)
  @Schema(
      description = "Application relevant detail, defined in the API or a common list",
      example = "1")
  public String code;

  @JsonProperty(value = "reason")
  @JsonInclude(content = JsonInclude.Include.NON_NULL)
  @Schema(
      description = "Explanation of the reason for the error which can be shown to a client user",
      example = "This customer does not exist")
  public String reason;

  @JsonProperty(value = "message")
  @JsonInclude(content = JsonInclude.Include.NON_NULL)
  @Schema(
      description =
          "More details and corrective actions related to the error which can be shown to a client user",
      example = "Please enter a smaller quantity")
  public String message;

  @JsonProperty(value = "status")
  @JsonInclude(content = JsonInclude.Include.NON_NULL)
  @Schema(description = "HTTP Error code extension", example = "400-2")
  public String status;

  @JsonProperty(value = "referenceError")
  @JsonInclude(content = JsonInclude.Include.NON_NULL)
  @Schema(
      description = "URI of documentation describing the error",
      example = "https://docs.microsoft.com/en-us/windows/desktop/wmdm/error-codes")
  public String referenceError;
}
