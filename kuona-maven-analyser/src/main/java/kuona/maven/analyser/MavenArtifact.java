package kuona.maven.analyser;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MavenArtifact {
    private final String groupId;
    private final String artifactId;
    private final String version;

    private final List<MavenArtifact> dependencies;
    private final List<MavenArtifact> parents;

    public MavenArtifact(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        dependencies = new ArrayList<>();
        parents = new ArrayList<>();
    }

    public boolean rootLevel() {
        return parents.size() == 0;
    }

    public boolean hasDependencies() {
        return dependencies.size() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MavenArtifact that = (MavenArtifact) o;

        if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) return false;
        if (artifactId != null ? !artifactId.equals(that.artifactId) : that.artifactId != null) return false;
        return !(version != null ? !version.equals(that.version) : that.version != null);

    }

    @Override
    public int hashCode() {
        int result = groupId != null ? groupId.hashCode() : 0;
        result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return groupId + '.' + artifactId;
    }

    public void addDependent(MavenArtifact artifact) {
        dependencies.add(artifact);
        artifact.addParent(this);
    }

    private void addParent(MavenArtifact artifact) {
        parents.add(artifact);
    }

    public void print(PrintStream out, int i, Predicate<? super MavenArtifact> predicate) {

        if (predicate.test(this)) {
            if (i > 3)
                return;

            for (int j = 0; j < i; j++)
                out.print(" ");
            out.println(toString());


            dependencies.stream().filter(predicate).forEach(p -> p.print(out, i + 3, predicate));
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public List<MavenArtifact> getDependencies() {
        return dependencies;
    }

    public String getArtifactId() {
        return artifactId;
    }
}
