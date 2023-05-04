package fr.arcep.tmf.util;

import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriInfo;
import java.util.Optional;

@ApplicationScoped
public class SortProcessor {

  public Optional<Sort> getSort(String sortQuery) {
    Optional<Sort> sort = Optional.empty();

    if (sortQuery != null && !sortQuery.isBlank()) {
      for (var s : sortQuery.split("\\s*,\\s*")) {
        if (s.isBlank()) {
          continue;
        }

        var direction = Direction.Ascending;
        if (s.startsWith("-")) {
          s = s.substring(1);
          direction = Direction.Descending;
        } else if (s.startsWith("+")) {
          s = s.substring(1);
        }

        if (sort.isEmpty()) {
          sort = Optional.of(Sort.by(s, direction));
        } else {
          sort.get().and(s, direction);
        }
      }
    }

    return sort;
  }

  public Optional<Sort> getSort(UriInfo uriInfo) {
    return getSort(uriInfo.getQueryParameters().getFirst("sort"));
  }
}
