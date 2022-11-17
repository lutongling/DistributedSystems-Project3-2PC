package utils;

import java.io.Serializable;

/**
 * This class represents the KV operations: GET, PUT and DELETE.
 */
public class KVOperation implements Serializable {
  private static final long serialVersionUID = 1l;

  public enum Type {
    GET, PUT, DELETE
  }

  private Type type;
  private String key;
  private String val;

  public KVOperation(Type type, String key, String val) {
    this.type = type;
    this.key = key;
    this.val = val;
  }

  public String getType() {
    return type.toString();
  }

  public String getKey() {
    return key;
  }

  public String getVal() {
    return val;
  }


}
