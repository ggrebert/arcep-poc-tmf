package fr.arcep.tmf.util.search;

import org.bson.Document;

public class SearchParser {

  public interface Operator {
    Document getQuery(String value);
  }
}
