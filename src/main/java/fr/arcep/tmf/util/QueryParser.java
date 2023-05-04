package fr.arcep.tmf.util;

import io.quarkus.logging.Log;
import jakarta.ws.rs.BadRequestException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.Document;

public enum QueryParser {
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

  private final Class<? extends QueryOperator> operatorClass;
  private final List<String> aliases;

  QueryParser(Class<? extends QueryOperator> operatorClass, List<String> aliases) {
    this.operatorClass = operatorClass;
    this.aliases = aliases;
  }

  public Class<? extends QueryOperator> getOperatorClass() {
    return operatorClass;
  }

  public List<String> getAliases() {
    return aliases;
  }

  public static QueryParser fromString(String operator) {
    for (QueryParser queryParser : QueryParser.values()) {
      if (queryParser.getAliases().contains(operator.trim().toLowerCase())) {
        return queryParser;
      }
    }

    throw new BadRequestException("Unknown operator: " + operator);
  }

  public static class Equals implements QueryOperator {

    @Override
    public Document getQuery(String value) {
      return new Document("$eq", typeConvert(value));
    }
  }

  public static class NotEquals implements QueryOperator {

    @Override
    public Document getQuery(String value) {
      return new Document("$ne", typeConvert(value));
    }
  }

  public static class GreaterThan implements QueryOperator {

    @Override
    public Document getQuery(String value) {
      return new Document("$gt", typeConvert(value));
    }
  }

  public static class GreaterThanOrEqual implements QueryOperator {

    @Override
    public Document getQuery(String value) {
      return new Document("$gte", typeConvert(value));
    }
  }

  public static class LessThan implements QueryOperator {

    @Override
    public Document getQuery(String value) {
      return new Document("$lt", typeConvert(value));
    }
  }

  public static class LessThanOrEqual implements QueryOperator {

    @Override
    public Document getQuery(String value) {
      return new Document("$lte", typeConvert(value));
    }
  }

  public static class Match implements QueryOperator {

    @Override
    public Document getQuery(String value) {
      return new Document("$regex", value);
    }
  }

  public static class NotMatch implements QueryOperator {

    @Override
    public Document getQuery(String value) {
      return new Document("$not", new Document("$regex", value));
    }
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

  public static class In implements QueryOperator {

    @Override
    public Document getQuery(String value) {
      var values =
          Pattern.compile("\\s*,\\s*").splitAsStream(value).map(QueryParser::typeConvert).toList();
      return new Document("$in", values);
    }
  }

  public static class NotIn implements QueryOperator {

    @Override
    public Document getQuery(String value) {
      var in = new In();
      return new Document("$not", in.getQuery(value));
    }
  }

  public static class Is implements QueryOperator {

    @Override
    @SuppressWarnings("java:S1192")
    public Document getQuery(String value) {
      switch (value.toLowerCase()) {
        case "null":
          return new Document("$eq", null);
        case "notnull":
        case "nnull":
          return new Document("$ne", null);
        case "exists":
          return new Document("$exists", true);
        case "notexists":
        case "nexists":
          return new Document("$exists", false);
        case "empty":
          return new Document("$size", 0);
        case "notempty":
        case "nempty":
          return new Document("$not", new Document("$size", 0));
        case "int":
        case "integer":
          return new Document("$type", "int");
        case "array":
          return new Document("$type", "array");
        case "object":
          return new Document("$type", "object");
        case "string":
          return new Document("$type", "string");
        case "bool":
        case "boolean":
          return new Document("$type", "bool");
        case "date":
          return new Document("$type", "date");
        default:
          break;
      }

      throw new IllegalArgumentException("Unknown value for in operator: " + value);
    }
  }

  public static class IsNot implements QueryOperator {

    @Override
    public Document getQuery(String value) {
      var is = new Is();
      return new Document("$not", is.getQuery(value));
    }
  }
}
