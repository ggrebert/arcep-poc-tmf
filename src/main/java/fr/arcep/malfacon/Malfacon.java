package fr.arcep.malfacon;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.arcep.tmf.model.TroubleTicket;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class Malfacon extends TroubleTicket {

  public static enum Status {
    NEW,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED
  }

  public Malfacon() {
    super();
    setType("MalfaconTroubleTicket");
  }

  @SuppressWarnings("java:S2387")
  public Status status;

  @JsonProperty(value = "mafalcon")
  @Valid
  @NotNull
  public MalfaconPayload malfaconPayload;

  public static class MalfaconPayload {

    @Min(3)
    public long volumetrie;

    public String localisation;

    public String defaut;

    public int quotePart;
  }
}
