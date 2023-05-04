package fr.arcep.tmf.util.operator;

import fr.arcep.tmf.util.TMFilter;
import org.bson.Document;

public class NotMatch implements TMFilter.SearchOperator {

  @Override
  public Document getQuery(String value) {
    var regex = new Match();
    return new Document("$not", regex.getQuery(value));
  }
}
