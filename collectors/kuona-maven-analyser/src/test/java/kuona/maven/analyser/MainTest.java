package kuona.maven.analyser;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MainTest {
    @Test
    public void acceptsPathsOnCommandline() {
        final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"some/path"});

        assertThat(options.getArgs().size(), is(1));
        assertThat(options.getArgs().get(0), is("some/path"));
    }

    @Test
    public void defaultIncludesEverything() {
        final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"some/path"});

        assertThat(options.getIncludeFilter().test(new MavenArtifact("anything", null, null)), is(true));
    }

    @Test
    public void acceptsIncludeFilterOption() {
        final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"--include", "foo", "dummy"});

        assertThat(options.getIncludeFilter().test(new MavenArtifact("anything", null, null)), is(false));
    }

    @Test
    public void acceptsAllArtifactsWithNoFilterOption() {
        final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"dummy"});

        assertThat(options.getArtifactFilter().test(new MavenArtifact(null, "anything", null)), is(true));
    }

    @Test
    public void acceptsArtifactFilterOption() {
        final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"--artifact", "bar", "dummy"});

        assertThat(options.getArtifactFilter().test(new MavenArtifact(null, "bar", null)), is(true));
        assertThat(options.getArtifactFilter().test(new MavenArtifact(null, "foo", null)), is(false));
    }

    @Test
    public void defaultsOutputFilename() {
        final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"dummy"});

        assertThat(options.getOutputFilename(), is("dependencies.json"));
    }

    @Test
    public void canSpecifyOutputFilename() {
        final AnalyserRuntimeOptions options = Main.parseOptions(new String[]{"--output", "foo.json", "dummy"});

        assertThat(options.getOutputFilename(), is("foo.json"));
    }


    @Test
    public void helpTest() {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Main.printHelp(outputStream);

        String result = new String(outputStream.toByteArray());

        assertThat(result, is("usage: java -jar kuona-maven-analyser.jar [options] <paths>\n" +
                "\n" +
                "Options\n" +
                "   -a,--artifact <arg>  Filter the output artifacts based on the supplied filter\n" +
                "                        pattern.\n" +
                "   -h,--help            Output this message\n" +
                "   -i,--include <arg>   Pattern used to filter all the dependencies in the\n" +
                "                        output\n" +
                "   -o,--output <arg>    Output filename\n" +
                "\n" +
                " See http://kuona.io\n"));
    }

    @Test
    public void canSpecifyOutput() {

    }
}
