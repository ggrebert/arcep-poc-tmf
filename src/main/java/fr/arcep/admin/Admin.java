package fr.arcep.admin;

import fr.arcep.tmf.model.TroubleTicket;

public class Admin extends TroubleTicket {

  public Admin() {
    super();
    setType("AdminTroubleTicket");
  }
}
