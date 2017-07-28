package kuona.maven.dot;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class MavenDotReaderTest {
  @org.junit.Test
  public void readsEmptyFile() throws Exception {

    InputStream stream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
    Map<String, Object> result = MavenDotReader.readDependencies(stream);

    assertThat(result.isEmpty(), org.hamcrest.CoreMatchers.is(true));
  }

  @Test
  public void readsEmptyDependencyFile() {
    final String input = "digraph \"com.grahambrooks:nio-socket-server:jar:1.0-SNAPSHOT\" {  }";
    InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    Map<String, Object> result = MavenDotReader.readDependencies(stream);

    Map<String, String> root = (Map<String, String>) result.get("root");
    assertThat(root.get("id"), equalTo("com.grahambrooks:nio-socket-server:jar:1.0-SNAPSHOT"));
  }

  @Test
  public void readsDependencies(){
    final String input = "digraph \"com.grahambrooks:nio-socket-server:jar:1.0-SNAPSHOT\" { \n" +
            "\t\"com.grahambrooks:nio-socket-server:jar:1.0-SNAPSHOT\" -> \"junit:junit:jar:4.12:test\" ; \n" +
            "\t\"com.grahambrooks:nio-socket-server:jar:1.0-SNAPSHOT\" -> \"org.assertj:assertj-core:jar:3.6.2:test\" ; \n" +
            "\t\"junit:junit:jar:4.12:test\" -> \"org.hamcrest:hamcrest-core:jar:1.3:test\" ; \n" +
            "}";

    InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    Map<String, Object> result = MavenDotReader.readDependencies(stream);

    List<Map> dependencies = (List<Map>) result.get("dependencies");
    assertThat(dependencies.size(), equalTo(3));

  }
}
