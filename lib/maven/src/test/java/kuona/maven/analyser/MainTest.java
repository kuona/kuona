package kuona.maven.analyser;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Maven Dependency Analyzer CLI tests")
public class MainTest {
  @Test
  public void acceptsPathsOnCommandline() {
    final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"some/path"});

    assertEquals(1, options.getArgs().size());
    assertEquals("some/path", options.getArgs().get(0));
  }

  @Test
  public void defaultIncludesEverything() {
    final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"some/path"});

    assertTrue(options.getIncludeFilter().test(new MavenArtifact("anything", null, null)));
  }

  @Test
  public void acceptsIncludeFilterOption() {
    final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"--include", "foo", "dummy"});

    assertFalse(options.getIncludeFilter().test(new MavenArtifact("anything", null, null)));
  }

  @Test
  public void acceptsAllArtifactsWithNoFilterOption() {
    final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"dummy"});

    assertTrue(options.getArtifactFilter().test(new MavenArtifact(null, "anything", null)));
  }

  @Test
  public void acceptsArtifactFilterOption() {
    final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"--artifact", "bar", "dummy"});

    assertTrue(options.getArtifactFilter().test(new MavenArtifact(null, "bar", null)));
    assertFalse(options.getArtifactFilter().test(new MavenArtifact(null, "foo", null)));
  }

  @Test
  public void defaultsOutputFilename() {
    final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"dummy"});

    assertEquals("dependencies.json", options.getOutputFilename());
  }

  @Test
  public void canSpecifyOutputFilename() {
    final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"--output", "foo.json", "dummy"});

    assertEquals("foo.json", options.getOutputFilename());
  }


  @Test
  public void helpTest() {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    Main.printHelp(outputStream);

    String result = new String(outputStream.toByteArray());

    assertEquals("usage: java -jar kuona-maven-analyser.jar [options] <paths>\n" +
      "\n" +
      "Options\n" +
      "   -a,--artifact <arg>  Filter the output artifacts based on the supplied filter\n" +
      "                        pattern.\n" +
      "   -h,--help            Output this message\n" +
      "   -i,--include <arg>   Pattern used to filter all the dependencies in the\n" +
      "                        output\n" +
      "   -o,--output <arg>    Output filename\n" +
      "\n" +
      " See http://kuona.io\n", result);
  }
}
