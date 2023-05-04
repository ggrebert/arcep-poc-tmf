package fr.arcep.tmf.util;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.bson.Document;

@ApplicationScoped
public class QueryFilterProcessor {

  private static final List<String> UNSEARCHABLE =
      List.of("limit", "offset", "sort", "fields", "filter");

  private static final Pattern MATCHER_OPERATOR =
      Pattern.compile("^(?<key>[^\\[]+)\\[(?<operator>[^\\]]+)\\]$");

  public void process(Document document, Map<String, List<String>> queryParameters) {
    Map<String, List<Filter>> filterMap = new HashMap<>();

    queryParameters.forEach(
        (key, values) -> {
          var matcher = MATCHER_OPERATOR.matcher(key);
          if (matcher.matches()) {
            var operator = QueryParser.fromString(matcher.group("operator"));
            var keyName = matcher.group("key");
            if (!UNSEARCHABLE.contains(keyName)) {
              initFilterMap(filterMap, keyName, operator, values);
            }
          } else if (!UNSEARCHABLE.contains(key)) {
            initFilterMap(filterMap, key, QueryParser.EQUAL, values);
          }
        });

    filterMap.forEach(
        (key, values) -> {
          if (values.size() == 1) {
            document.put(key, values.get(0).process());
          } else {
            var doc = new Document();
            values.stream().map(Filter::process).forEach(d -> d.forEach(doc::put));
            document.put(key, doc);
          }
        });
  }

  private void initFilterMap(
      Map<String, List<Filter>> filterMap, String key, QueryParser operator, List<String> value) {
    var params = filterMap.computeIfAbsent(key, k -> new ArrayList<>());
    params.addAll(value.stream().map(v -> new Filter(operator, v)).toList());
  }

  private static class Filter {
    private final QueryParser operator;
    private final String value;

    public Filter(QueryParser operator, String value) {
      this.operator = operator;
      this.value = value;
    }

    public Document process() {
      try {
        return operator.getOperatorClass().getConstructor().newInstance().getQuery(value);
      } catch (Exception e) {
        Log.errorf(
            "can't instantiate operator '%s' : %s",
            operator.getOperatorClass().getName(), e.getLocalizedMessage());
        throw new BadRequestException(
            "can't instantiate operator '" + operator.getOperatorClass().getName() + "'");
      }
    }
  }
}
