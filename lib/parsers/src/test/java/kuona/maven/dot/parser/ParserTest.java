package kuona.maven.dot.parser;

import kuona.maven.dot.parser.MavenDotParser.ArtifactContext;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ParserTest {
  @Test
  public void recognisesArtifact() {

    ArtifactContext context = parseArtifact("\"a:b:jar:1.0:test\"");

    assertThat(context.groupid.getText(), equalTo("a"));
    assertThat(context.artifactid.getText(), equalTo("b"));
    assertThat(context.packageing.getText(), equalTo("jar"));
    assertThat(context.version.getText(), equalTo("1.0"));
    assertThat(context.scope.getText(), equalTo("test"));
  }

  @Test
  public void recognisesArtifactWithoutScope() {

    ArtifactContext context = parseArtifact("\"a:b:jar:1.0\"");

    assertThat(context.groupid.getText(), equalTo("a"));
    assertThat(context.artifactid.getText(), equalTo("b"));
    assertThat(context.packageing.getText(), equalTo("jar"));
    assertThat(context.version.getText(), equalTo("1.0"));
    assertThat(context.scope, nullValue());
  }

  private ArtifactContext parseArtifact(String input) {
    MavenDotLexer lexer = new MavenDotLexer(CharStreams.fromString(input));
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    MavenDotParser parser = new MavenDotParser(tokenStream);
    return parser.artifact();
  }

  @Test
  public void readsArtifactWithSpecialCharacters() {

    ArtifactContext context = parseArtifact("\"p2.eclipse-plugin:org.openhab.core.compat1x:jar:lib/jackson-core-asl-1.9.2.jar:2.2.0.201706290927:system\" ;");

    assertThat(context.groupid.getText(), equalTo("p2.eclipse-plugin"));
    assertThat(context.artifactid.getText(), equalTo("org.openhab.core.compat1x"));
    assertThat(context.packageing.getText(), equalTo("jar"));
    assertThat(context.version.getText(), equalTo("2.2.0.201706290927"));
    assertThat(context.scope.getText(), equalTo("system"));
  }
}
