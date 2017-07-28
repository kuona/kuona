package kuona.maven.dot.parser;

import kuona.maven.dot.parser.MavenDotLexer;
import kuona.maven.dot.parser.MavenDotParser;
import kuona.maven.dot.parser.MavenDotParser.ArtifactContext;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ParserTest {
    @Test
    public void recognisesArtifact() {

        MavenDotLexer lexer = new MavenDotLexer(CharStreams.fromString("\"a:b:jar:1.0:test\""));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        MavenDotParser parser = new MavenDotParser(tokenStream);
        ArtifactContext context = parser.artifact();

        assertThat(context.groupid.getText(), equalTo("a"));
        assertThat(context.artifactid.getText(), equalTo("b"));
        assertThat(context.packageing.getText(), equalTo("jar"));
        assertThat(context.version.getText(), equalTo("1.0"));
        assertThat(context.scope.getText(), equalTo("test"));
    }
}
