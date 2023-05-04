package fr.arcep.tmf.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class FieldProcessor {

  public Map<String, Object> filter(Map<String, Object> map, UriInfo uriInfo) {
    var fields = uriInfo.getQueryParameters().get("fields");
    return filter(map, fields);
  }

  public Map<String, Object> filter(Map<String, Object> map, List<String> fields) {
    if (fields.isEmpty()) {
      return map;
    }

    var showedFields =
        fields.stream()
            .map(f -> Arrays.asList(f.split("\\s*,\\s*")))
            .flatMap(List::stream)
            .collect(Collectors.toList());

    return map.entrySet().stream()
        .filter(e -> showedFields.contains(e.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
