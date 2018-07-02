package kuona.query;

import java.util.Objects;

class QueryFilter {
  String key;
  String value;

  public QueryFilter(String key, String value) {

    this.key = key;
    this.value = value;
  }

  @Override
  public String toString() {
    return "QueryFilter{" +
      "key='" + key + '\'' +
      ", value='" + value + '\'' +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    QueryFilter that = (QueryFilter) o;
    return Objects.equals(key, that.key) &&
      Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {

    return Objects.hash(key, value);
  }
}
