package fr.arcep.anomalieadresse;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.arcep.tmf.model.TroubleTicket;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class AnomalieAdresse extends TroubleTicket {

  public AnomalieAdresse() {
    super();
    setType("AnomalieAdresseTroubleTicket");
  }

  @JsonProperty(value = "payload")
  @Valid
  public Payload payload;

  public static class Address {

    public String street;

    @NotBlank public String city;

    @NotBlank public String zip;

    public String country = "France";
    public String state;
    public String number;
    public String complement;
  }

  @Schema(name = "AnomalieAdressePayload")
  public static class Payload {

    @JsonProperty(value = "commentaire_oc")
    @Schema(description = "Commentaire de l'OC")
    public String commentaireOc;

    @JsonProperty(value = "commande_client_final_oc", required = true)
    @Schema(description = "Commande client final OC", required = true)
    public boolean commandeClientFinalOc;

    @JsonProperty(value = "adresse")
    @Schema(description = "Adresse")
    @Valid
    public Address address;
  }
}
