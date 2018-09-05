package kuona.maven.analyser.model;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Element serialization tests")
public class ElementTest {
  @Test
  @DisplayName("Elements are serializable")
  public void ElementsSerialize() {

    assertEquals("{\"name\":\"Foo\",\"children\":[]}", new Gson().toJson(new FlareNode("Foo")).toString());
  }
}
