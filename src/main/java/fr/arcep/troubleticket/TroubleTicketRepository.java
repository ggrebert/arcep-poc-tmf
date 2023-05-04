package fr.arcep.troubleticket;

import fr.arcep.tmf.util.RepositoryBase;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import org.eclipse.microprofile.reactive.messaging.Channel;

@ApplicationScoped
public class TroubleTicketRepository implements RepositoryBase<TroubleTicketEntity> {

  @Channel("trouble-ticket-create")
  MutinyEmitter<TroubleTicketNotification> createEmitter;

  @Channel("trouble-ticket-delete")
  MutinyEmitter<String> deleteEmitter;

  @Override
  public String getMeterPrefix() {
    return "troubleTickets";
  }

  @Override
  public TroubleTicketEntity fromMap(Map<String, Object> map) {
    return TroubleTicketEntity.fromMap(map);
  }

  @Override
  public Uni<Void> notifyCreate(TroubleTicketEntity entity) {
    return createEmitter.send(new TroubleTicketNotification(entity));
  }

  @Override
  public Uni<Void> notifyDelete(TroubleTicketEntity entity) {
    return deleteEmitter.send(entity.id);
  }
}
