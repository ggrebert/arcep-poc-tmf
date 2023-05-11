package fr.arcep.tmf.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.arc.Arc;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Map;

public interface RepositoryBase<E extends EntityBase>
    extends ReactivePanacheMongoRepositoryBase<E, String> {

  public String getMeterPrefix();

  public E fromMap(Map<String, Object> map);

  default Uni<Void> notifyCreate(E entity) {
    return Uni.createFrom().voidItem();
  }

  default Uni<Void> notifyDelete(E entity) {
    return Uni.createFrom().voidItem();
  }

  default void validate(E entity) {
    var validator = Arc.container().instance(Validator.class).get();
    var violations = validator.validate(entity);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }

  default void meterCount(String name, String domain) {
    var meter = Arc.container().instance(MeterRegistry.class).get();
    meter.counter(getMeterPrefix() + "." + name, "clientId", domain).increment();
  }

  default E transform(E entity) {
    return entity;
  }

  default Uni<E> persistAndNotify(Map<String, Object> data, String domain) {
    return Uni.createFrom()
        .item(data)
        .map(this::fromMap)
        .map(this::transform)
        .invoke(t -> t.domain = domain)
        .invoke(this::validate)
        .chain(this::persist)
        .call(this::notifyCreate)
        .invoke(() -> meterCount("create", domain));
  }

  default Uni<Void> deleteAndNotify(E entity) {
    return delete(entity)
        .call(() -> this.notifyDelete(entity))
        .invoke(() -> meterCount("delete", entity.domain));
  }
}
