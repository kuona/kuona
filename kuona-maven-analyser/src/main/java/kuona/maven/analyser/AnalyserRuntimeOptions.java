package kuona.maven.analyser;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class AnalyserRuntimeOptions {
    public static final Predicate<MavenArtifact> MATCH_ANY = (x) -> true;
    private List<String> args;
    private Predicate<MavenArtifact> includeFilter;
    private Predicate<MavenArtifact> artifactFilter;
    private String outputFilename;


    public AnalyserRuntimeOptions() {
        includeFilter = MATCH_ANY;
        artifactFilter = MATCH_ANY;
        outputFilename = "dependencies.json";
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = Arrays.asList(args);
    }

    public Predicate<MavenArtifact> getIncludeFilter() {
        return includeFilter;
    }

    public void setInclude(String include) {
        this.includeFilter = (s) -> Pattern.compile(include + ".*").matcher(s.getGroupId()).matches();
    }

    public Predicate<MavenArtifact> getArtifactFilter() {
        return artifactFilter;
    }

    public void setArtifactFilter(String artifactPattern) {
        this.artifactFilter = (s) -> Pattern.compile(artifactPattern + ".*").matcher(s.getArtifactId()).matches();
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }
}
