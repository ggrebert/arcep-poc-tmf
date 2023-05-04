package fr.arcep.tmf.util.operator;

import fr.arcep.tmf.util.TMFilter;
import java.util.regex.Pattern;
import org.bson.Document;

public class In implements TMFilter.SearchOperator {

  @Override
  public Document getQuery(String value) {
    var values =
        Pattern.compile("\\s*,\\s*").splitAsStream(value).map(TMFilter::typeConvert).toList();
    return new Document("$in", values);
  }
}
