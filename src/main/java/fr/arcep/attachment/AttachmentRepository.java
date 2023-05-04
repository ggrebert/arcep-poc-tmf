package fr.arcep.attachment;

import fr.arcep.tmf.util.RepositoryBase;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import org.eclipse.microprofile.reactive.messaging.Channel;

@ApplicationScoped
public class AttachmentRepository implements RepositoryBase<AttachmentEntity> {

  @Channel("attachment-create")
  MutinyEmitter<AttachmentEntity> createEmitter;

  @Channel("attachment-delete")
  MutinyEmitter<String> deleteEmitter;

  @Override
  public String getMeterPrefix() {
    return "attachments";
  }

  @Override
  public AttachmentEntity fromMap(Map<String, Object> map) {
    return AttachmentEntity.fromMap(map);
  }

  @Override
  public Uni<Void> notifyCreate(AttachmentEntity entity) {
    return createEmitter.send(entity);
  }

  @Override
  public Uni<Void> notifyDelete(AttachmentEntity entity) {
    return deleteEmitter.send(entity.id);
  }
}
