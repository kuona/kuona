package kuona.query;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserTest {

  @Test
  @DisplayName("Accepts an empty query")
  void acceptsEmptyQuery() {
    Query result = Parser.parse(stringToStream(""));

    assertTrue(result.isEmpty());
  }

  private ByteArrayInputStream stringToStream(String s) {
    return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
  }

  @Nested
  @DisplayName("Query term tests")
  class TermOnlyQueries {
    @Test
    @DisplayName("Accepts a single term")
    void acceptsASingleTerm() {
      Query query = Parser.parse(stringToStream("term"));
      Assertions.assertIterableEquals(asList("term"), query.getTerms());
    }

    @Test
    @DisplayName("Accepts multiple terms")
    void acceptsMultipleTerms() {
      Query query = Parser.parse(stringToStream("termA termB"));
      Assertions.assertIterableEquals(asList("termA", "termB"), query.getTerms());
    }

  }

  @Nested
  @DisplayName("Query filter tests")
  class FilterQueries {
    @Test
    @DisplayName("Accepts a filter")
    void acceptsAFilter() {
      Query query = Parser.parse(stringToStream("key1:value1"));
      Assertions.assertIterableEquals(asList(), query.getTerms());
      assertEquals(new QueryFilter("key1", "value1"), query.getFilters().iterator().next());
    }

    @Test
    @DisplayName("Accepts multiple filters")
    void acceptsMultipleFilters() {
      Query query = Parser.parse(stringToStream("key1:value1 key2:value2"));
      Assertions.assertIterableEquals(asList(), query.getTerms());
      Iterator<QueryFilter> filters = query.getFilters().iterator();
      assertEquals(new QueryFilter("key1", "value1"), filters.next());
      assertEquals(new QueryFilter("key2", "value2"), filters.next());
    }
  }

  @Nested
  @DisplayName("Complex queries")
  class ComplexQueries {
    @Test
    @DisplayName("Term and filter")
    void termsAndFilters() {
      Query query = Parser.parse(stringToStream("term key1:value1"));
      Assertions.assertIterableEquals(asList("term"), query.getTerms());
      Iterator<QueryFilter> filters = query.getFilters().iterator();
      assertEquals(new QueryFilter("key1", "value1"), filters.next());

    }
  }
}
