package fr.arcep.tmf.util;

import org.bson.Document;

public interface QueryOperator {

  Document getQuery(String value);
}
