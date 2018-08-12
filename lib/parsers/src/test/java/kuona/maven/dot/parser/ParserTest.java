package kuona.maven.dot.parser;

import kuona.maven.dot.parser.MavenDotParser.ArtifactContext;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Maven pom parser")
class ParserTest {
  @Test
  @DisplayName("Recognises artifact references")
  void recognisesArtifact() {

    ArtifactContext context = parseArtifact("\"a:b:jar:1.0:test\"");

    assertEquals("a", context.groupid.getText());
    assertEquals("b", context.artifactid.getText());
    assertEquals("jar", context.packageing.getText());
    assertEquals("1.0", context.version.getText());
    assertEquals("test", context.scope.getText());
  }

  @Test
  @DisplayName("Recognises artifact references without a scope reference")
  void recognisesArtifactWithoutScope() {

    ArtifactContext context = parseArtifact("\"a:b:jar:1.0\"");

    assertEquals("a", context.groupid.getText());
    assertEquals("b", context.artifactid.getText());
    assertEquals("jar", context.packageing.getText());
    assertEquals("1.0", context.version.getText());
    assertNull(context.scope);
  }

  private ArtifactContext parseArtifact(String input) {
    MavenDotLexer lexer = new MavenDotLexer(CharStreams.fromString(input));
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    MavenDotParser parser = new MavenDotParser(tokenStream);
    return parser.artifact();
  }

  @Test
  void readsArtifactWithSpecialCharacters() {

    ArtifactContext context = parseArtifact("\"p2.eclipse-plugin:org.openhab.core.compat1x:jar:lib/jackson-core-asl-1.9.2.jar:2.2.0.201706290927:system\" ;");

    assertEquals("p2.eclipse-plugin", context.groupid.getText());
    assertEquals("org.openhab.core.compat1x", context.artifactid.getText());
    assertEquals("jar", context.packageing.getText());
    assertEquals("2.2.0.201706290927", context.version.getText());
    assertEquals("system", context.scope.getText());
  }
}
