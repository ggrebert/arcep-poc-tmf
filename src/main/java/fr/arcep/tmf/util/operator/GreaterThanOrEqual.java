package fr.arcep.tmf.util.operator;

import fr.arcep.tmf.util.TMFilter;
import org.bson.Document;

public class GreaterThanOrEqual implements TMFilter.SearchOperator {

  @Override
  public Document getQuery(String value) {
    return new Document("$gte", TMFilter.typeConvert(value));
  }
}
