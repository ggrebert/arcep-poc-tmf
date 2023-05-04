package fr.arcep.tmf.util.operator;

import fr.arcep.tmf.util.TMFilter;
import org.bson.Document;

public class IsNot implements TMFilter.SearchOperator {

  @Override
  public Document getQuery(String value) {
    var is = new Is();
    return new Document("$not", is.getQuery(value));
  }
}
