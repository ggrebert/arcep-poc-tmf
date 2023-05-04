package fr.arcep.tmf.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arcep.tmf.model.Base;
import io.quarkus.arc.Arc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonExtraElements;
import org.bson.codecs.pojo.annotations.BsonId;

public class EntityBase extends Base {

  public static final String PAYLOAD_FIELD = "payload";
  public static final String DOMAIN_FIELD = "domain";
  public static final List<String> BANNED_FIELDS = List.of(PAYLOAD_FIELD, DOMAIN_FIELD);

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @BsonId
  public String id = UUID.randomUUID().toString();

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public Date creationDate = new Date();

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public Date lastUpdate;

  public String domain;

  @BsonExtraElements public Document payload = new Document();

  @SuppressWarnings("unchecked")
  public Map<String, Object> toMap() {
    var om = Arc.container().instance(ObjectMapper.class).get();
    Map<String, Object> map = om.convertValue(this, Map.class);

    var extraData = map.getOrDefault(PAYLOAD_FIELD, new HashMap<String, Object>());
    getBannedFields().forEach(map::remove);
    ((Map<String, Object>) extraData).forEach(map::put);

    return map;
  }

  public static <E extends EntityBase> E fromMap(
      Map<String, Object> map, Class<E> toValueType, List<String> ignoredFields) {
    ignoredFields.forEach(map::remove);

    var newMap = new HashMap<String, Object>();
    var payload = new HashMap<String, Object>();
    var fields = new ArrayList<String>();

    Arrays.asList(toValueType.getDeclaredFields())
        .forEach(
            f -> {
              var name = f.getName();
              fields.add(name);
              if (map.containsKey(name) && map.get(name) != null) {
                if (BANNED_FIELDS.contains(name)) {
                  payload.put(name, map.get(name));
                } else {
                  newMap.put(name, map.get(name));
                }
              }
            });

    map.forEach(
        (k, v) -> {
          if (!fields.contains(k)) {
            payload.put(k, v);
          }
        });

    newMap.put(PAYLOAD_FIELD, payload);

    var om = Arc.container().instance(ObjectMapper.class).get();
    return om.convertValue(newMap, toValueType);
  }

  protected List<String> getBannedFields() {
    return List.of(PAYLOAD_FIELD, DOMAIN_FIELD);
  }
}
