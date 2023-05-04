package fr.arcep.tmf.util;

import fr.arcep.tmf.util.operator.Equals;
import fr.arcep.tmf.util.operator.GreaterThan;
import fr.arcep.tmf.util.operator.GreaterThanOrEqual;
import fr.arcep.tmf.util.operator.In;
import fr.arcep.tmf.util.operator.Is;
import fr.arcep.tmf.util.operator.IsNot;
import fr.arcep.tmf.util.operator.LessThan;
import fr.arcep.tmf.util.operator.LessThanOrEqual;
import fr.arcep.tmf.util.operator.Match;
import fr.arcep.tmf.util.operator.NotEquals;
import fr.arcep.tmf.util.operator.NotIn;
import fr.arcep.tmf.util.operator.NotMatch;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bson.Document;

@ApplicationScoped
public class TMFilter {

  /** List of query parameters which can not be searchable */
  public static final List<String> UNSEARCHABLE =
      List.of("limit", "offset", "sort", "fields", "filter");

  private static final Pattern MATCHER_OPERATOR =
      Pattern.compile("^(?<key>[^\\[]+)\\[(?<operator>[^\\]]+)\\]$");

  /**
   * Filter fields of a map with the query parameter "fields"
   *
   * @param map The map to filter
   * @param uriInfo
   * @return A reduced map
   */
  public Map<String, Object> filterFields(Map<String, Object> map, UriInfo uriInfo) {
    var fields = uriInfo.getQueryParameters().get("fields");
    return filterFields(map, fields);
  }

  /**
   * Filter fields of a map
   *
   * @param map The map to filter
   * @param fields
   * @return A reduced map
   */
  public Map<String, Object> filterFields(Map<String, Object> map, List<String> fields) {
    if (fields == null || fields.isEmpty()) {
      return map;
    }

    var showedFields =
        fields.stream()
            .map(f -> Arrays.asList(f.split("\\s*,\\s*")))
            .flatMap(List::stream)
            .filter(f -> !f.isBlank())
            .collect(Collectors.toList());

    if (showedFields.isEmpty()) {
      return map;
    }

    return map.entrySet().stream()
        .filter(e -> showedFields.contains(e.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Get the sort from the query parameter "sort"
   *
   * @param sortQuery
   * @return
   */
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

  /**
   * Get the sort from the query parameter "sort"
   *
   * @param uriInfo
   * @return
   */
  public Optional<Sort> getSort(UriInfo uriInfo) {
    return getSort(uriInfo.getQueryParameters().getFirst("sort"));
  }

  public Document process(Document document, MultivaluedMap<String, String> queryParameters) {
    var filterMap = new MultivaluedHashMap<String, Filter>();

    queryParameters.forEach(
        (key, values) -> {
          var matcher = MATCHER_OPERATOR.matcher(key);
          if (matcher.matches()) {
            var operator = SearchParser.fromString(matcher.group("operator"));
            var keyName = matcher.group("key");
            if (!UNSEARCHABLE.contains(keyName)) {
              initFilterMap(filterMap, keyName, operator, values);
            }
          } else if (!UNSEARCHABLE.contains(key)) {
            initFilterMap(filterMap, key, values);
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

    Log.debug("Mongo filter: " + document);

    return document;
  }

  public static Object typeConvert(String value) {
    try {
      if (value.matches("^-?\\d+$")) {
        return Double.parseDouble(value);
      } else if (value.matches("^-?\\d+\\.\\d+$")) {
        return Double.parseDouble(value);
      } else if (value.matches("^-?\\d+\\.\\d+e-?\\d+$")) {
        return Double.parseDouble(value);
      } else if (value.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
        return Date.from(LocalDate.parse(value).atStartOfDay(ZoneId.systemDefault()).toInstant());
      } else if (value.matches("^\\d{4}-\\d{2}-\\d{2}")) {
        return Date.from(LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant());
      } else if ("TRUE".equalsIgnoreCase(value)) {
        return true;
      } else if ("FALSE".equalsIgnoreCase(value)) {
        return false;
      }
    } catch (Exception e) {
      Log.warn("Unable to convert value to type: '" + value + "' (" + e.getMessage() + ")");
    }

    return value;
  }

  public interface SearchOperator {
    Document getQuery(String key);
  }

  public enum SearchParser {
    EQUAL(Equals.class, List.of("eq", "==")),
    NOT_EQUAL(NotEquals.class, List.of("ne", "!=", "<>")),
    GREATER_THAN(GreaterThan.class, List.of("gt", ">")),
    GREATER_THAN_OR_EQUAL(GreaterThanOrEqual.class, List.of("gte", ">=")),
    LESS_THAN(LessThan.class, List.of("lt", "<")),
    LESS_THAN_OR_EQUAL(LessThanOrEqual.class, List.of("lte", "<=")),
    MATCH(Match.class, List.of("match", "regex", "=~")),
    NOT_MATCH(NotMatch.class, List.of("notmatch", "notregex", "!~")),
    IS(Is.class, List.of("is", "=")),
    IS_NOT(IsNot.class, List.of("isnot", "isnt", "not", "nis", "!")),
    IN(In.class, List.of("in")),
    NOT_IN(NotIn.class, List.of("nin", "notin")),
    ;

    private final Class<? extends SearchOperator> operatorClass;
    private final List<String> aliases;

    SearchParser(Class<? extends SearchOperator> operatorClass, List<String> aliases) {
      this.operatorClass = operatorClass;
      this.aliases = aliases;
    }

    public Class<? extends SearchOperator> getOperatorClass() {
      return operatorClass;
    }

    public List<String> getAliases() {
      return aliases;
    }

    public static SearchParser fromString(String operator) {
      for (var parser : SearchParser.values()) {
        if (parser.getAliases().contains(operator.trim().toLowerCase())) {
          return parser;
        }
      }

      throw new BadRequestException("Unknown operator: " + operator);
    }
  }

  private void initFilterMap(
      MultivaluedMap<String, Filter> filterMap,
      String key,
      SearchParser operator,
      List<String> value) {
    value.stream().map(v -> new Filter(operator, v)).forEach(f -> filterMap.add(key, f));
  }

  private void initFilterMap(
      MultivaluedMap<String, Filter> filterMap, String key, List<String> value) {
    initFilterMap(filterMap, key, SearchParser.EQUAL, value);
  }

  private static class Filter {
    private final SearchParser operator;
    private final String value;

    public Filter(SearchParser operator, String value) {
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
