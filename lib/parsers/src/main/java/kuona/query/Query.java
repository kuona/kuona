package kuona.query;

import java.util.ArrayList;

public class Query {

  private final ArrayList terms = new ArrayList<String>();
  private final ArrayList filters = new ArrayList();

  public void addTerm(String term) {
    terms.add(term);
  }

  public void addFilter(String key, String value) {
    filters.add(new QueryFilter(key, value));
  }

  public boolean isEmpty() {
    return terms.isEmpty();
  }

  public Iterable<String> getTerms() {
    return terms;
  }

  public Iterable<QueryFilter> getFilters() {
    return filters;
  }

}
