package fr.arcep.ext;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arcep.attachment.AttachmentEntity;
import fr.arcep.malfacon.Malfacon;
import fr.arcep.troubleticket.TroubleTicketNotification;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@ApplicationScoped
public class Listner {

  @Inject S3AsyncClient s3;

  @Inject ObjectMapper mapper;

  @Incoming("attachment-delete-s3")
  public Uni<Void> deleteAttachment(AttachmentEntity entity) {
    var s3Request = DeleteObjectRequest.builder().bucket(entity.domain).key(entity.id).build();

    return Uni.createFrom()
        .completionStage(() -> s3.deleteObject(s3Request))
        .onItem()
        .invoke(
            r -> {
              if (r.sdkHttpResponse().isSuccessful()) {
                Log.infof("Attachment %s deleted", entity.id);
              } else {
                Log.errorf("Attachment %s not deleted", entity.id);
              }
            })
        .onFailure()
        .invoke(t -> Log.errorf(t, "Attachment %s not deleted", entity.id))
        .replaceWithVoid();
  }

  @Incoming("troubleticket-created")
  public Uni<Void> troubleTicketCreated(TroubleTicketNotification tt) {
    return Uni.createFrom()
        .item(tt)
        .invoke(t -> Log.infof("TroubleTicket %s created in domain %s", t.id, t.domain))
        .invoke(t -> Log.infof("Received message from trouble ticket: %s", messageToString(t)))
        .replaceWithVoid();
  }

  @Incoming("troubleticket-deleted")
  public Uni<Void> troubleTicketDeleted(UUID id) {
    return Uni.createFrom()
        .item(id)
        .invoke(t -> Log.infof("Trouble ticket %s deleted", id))
        .replaceWithVoid();
  }

  @Incoming("malfacon-created")
  public Uni<Void> malfaconCreated(Malfacon malfacon) {
    return Uni.createFrom()
        .item(malfacon)
        .invoke(t -> Log.infof("Mafacon %s created", t.id))
        .invoke(t -> Log.infof("Received message from malfacon: %s", messageToString(t)))
        .replaceWithVoid();
  }

  @Incoming("malfacon-deleted")
  public Uni<Void> malfaconDeleted(UUID id) {
    return Uni.createFrom()
        .item(id)
        .invoke(t -> Log.infof("Mafacon %s deleted", id))
        .replaceWithVoid();
  }

  private String messageToString(Object message) {
    try {
      return mapper.writeValueAsString(message);
    } catch (Exception e) {
      return message.toString();
    }
  }
}
