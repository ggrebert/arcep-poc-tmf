package fr.arcep.eintervention.model;

import fr.arcep.tmf.model.TroubleTicket;
import java.util.Date;
import java.util.List;

public class TroubleTicketIntervention extends TroubleTicket {

  public List<InterventionDO.NaturePboPto> naturePboPto;
  public String pm;
  public String codeDO;
  public String codeOI;
  public Date debutInter;
  public Date finInter;

  public TroubleTicketIntervention() {
    super();
    this.name = "InterventionDO";
  }

  public TroubleTicketIntervention(InterventionDO intervention) {
    super();
    this.name = "InterventionDO";
    this.externalId = intervention.refDO;
    this.codeDO = intervention.codeDO;
    this.codeOI = intervention.codeOI;
    this.pm = intervention.pm;
    this.debutInter = intervention.debutInter;
    this.finInter = intervention.finInter;
    this.naturePboPto = intervention.naturePboPto;
  }

  public InterventionDO.Response toResponse() {
    var r = new InterventionDO.Response();
    r.id = this.id;
    return r;
  }
}
