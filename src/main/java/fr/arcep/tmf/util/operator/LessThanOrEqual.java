package fr.arcep.tmf.util.operator;

import fr.arcep.tmf.util.TMFilter;
import org.bson.Document;

public class LessThanOrEqual implements TMFilter.SearchOperator {

  @Override
  public Document getQuery(String value) {
    return new Document("$lte", TMFilter.typeConvert(value));
  }
}
