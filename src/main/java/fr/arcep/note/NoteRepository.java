package fr.arcep.note;

import fr.arcep.tmf.util.RepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class NoteRepository implements RepositoryBase<NoteEntity> {

  @Override
  public String getMeterPrefix() {
    return "notes";
  }

  @Override
  public NoteEntity fromMap(Map<String, Object> map) {
    return NoteEntity.fromMap(map);
  }
}
