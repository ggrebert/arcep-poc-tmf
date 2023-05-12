package fr.arcep.eintervention.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.Length;

public class InterventionDO {

  @JsonProperty("Code_DO")
  @NotBlank
  @Pattern(regexp = "^[A-Z0-9]{4}$")
  @Schema(
      pattern = "^[A-Z0-9]{4}$",
      example = "FREE",
      description = "Code opérateur Interop du donneur d'ordre")
  public String codeDO;

  @JsonProperty("Code_OI")
  @NotBlank
  @Pattern(regexp = "^[A-Z0-9]{4}$")
  @Schema(
      pattern = "^[A-Z0-9]{4}$",
      example = "SFR0",
      description = "Code opérateur Interop de l'opérateur d'infrastructure")
  public String codeOI;

  @JsonProperty("Ref_Interv_DO")
  @NotBlank
  @Length(max = 50)
  @Schema(
      maxLength = 50,
      description = "Référence de l'intervention chez le donneur d'ordre",
      example = "5b9ae1de-8742-47f1-81a2-9230634b667f")
  public String refDO;

  @JsonProperty("PM")
  @NotBlank
  @Length(max = 50)
  @Schema(maxLength = 50, example = "FI-91691-000X", description = "Référence réglementaire du PM")
  public String pm;

  @JsonProperty("Debut_Inter")
  @NotNull
  @Schema(
      description = "Date du début de l'intervention",
      format = "date-time",
      example = "2021-11-25T11:00:00.000Z")
  public Date debutInter;

  @JsonProperty("Fin_Inter")
  @Schema(
      description = "Date de fin de l'intervention",
      format = "date-time",
      example = "2021-11-25T11:40:00.000Z")
  public Date finInter;

  @Valid
  @NotNull
  @Size(min = 1)
  public List<NaturePboPto> naturePboPto;

  public static class NaturePboPto {

    @NotNull
    @Min(1)
    @Max(6)
    @Schema(
        enumeration = {"1", "2", "3", "4", "5", "6"},
        description =
            """
        Nature finale de l'intervention.

        Valeurs possibles :

        * 1 => RACC PTO à construire
        * 2 => RACC PTO existante
        * 3 => RACC hotline
        * 4 => SAV OC
        * 5 => SAV OI
        * 6 => NON FOURNIE (valeur possible permettant de rendre facultatif la fourniture de l'information)
        """)
    @JsonProperty("nature")
    public Integer nature;

    @Length(max = 100)
    @Schema(description = "Référence PBO si disponible", example = "PTXXXXXXX", maxLength = 100)
    @JsonProperty("PBO")
    public String pbo;

    @Length(max = 100)
    @Schema(description = "Référence PTO si disponible", example = "FI-YYYY-YYYY", maxLength = 100)
    @JsonProperty("PTO")
    public String pto;
  }

  public static class Response {

    @JsonProperty("Code_Reponse")
    public Integer code = 0;

    @JsonProperty("Libelle_Reponse")
    public String libelle = "OK";

    @JsonProperty("Ref_Interv_OI")
    public String id;
  }
}
