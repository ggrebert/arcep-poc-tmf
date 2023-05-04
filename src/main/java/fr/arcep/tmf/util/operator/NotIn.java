package fr.arcep.tmf.util.operator;

import fr.arcep.tmf.util.TMFilter;
import org.bson.Document;

public class NotIn implements TMFilter.SearchOperator {

  @Override
  public Document getQuery(String value) {
    var in = new In();
    return new Document("$not", in.getQuery(value));
  }
}
