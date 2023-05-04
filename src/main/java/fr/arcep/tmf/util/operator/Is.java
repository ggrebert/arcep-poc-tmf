package fr.arcep.tmf.util.operator;

import fr.arcep.tmf.util.TMFilter;
import org.bson.Document;

public class Is implements TMFilter.SearchOperator {

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
