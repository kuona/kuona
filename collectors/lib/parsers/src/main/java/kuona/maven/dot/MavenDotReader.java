package kuona.maven.dot;


import kuona.maven.dot.parser.MavenDotLexer;
import kuona.maven.dot.parser.MavenDotParser;
import kuona.maven.dot.parser.MavenDotParser.GraphContext;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MavenDotReader {
    public static Map<String, Object> readDependencies(InputStream stream) {
        HashMap<String, Object> result = new HashMap<>();

        try {
            MavenDotLexer lexer = new MavenDotLexer(CharStreams.fromStream(stream));
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            MavenDotParser parser = new MavenDotParser(tokenStream);
            GraphContext context = parser.graph();

            kuona.maven.dot.parser.MavenDotParser.ArtifactContext artifact = context.artifact();
            if (context.artifact() != null) {
                result.put("root", artifactAsMap(artifact));
            }

            ArrayList<Map> dependencies = new ArrayList<>();
            for (MavenDotParser.DependencyContext dependencyContext : context.dependency()) {
                dependencies.add(dependencyAsMap(dependencyContext));
            }

            if (dependencies.size() > 0) {
                result.put("dependencies", dependencies);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static Map<String, Object> dependencyAsMap(MavenDotParser.DependencyContext dependency) {
        return new HashMap<String, Object>() {{
            put("from", artifactAsMap(dependency.from));
            put("to", artifactAsMap(dependency.to));
        }};
    }

    private static HashMap<String, String> artifactAsMap(MavenDotParser.ArtifactContext artifact) {
        String quotedId = artifact.getText();
        return new HashMap<String, String>() {{
            put("id", quotedId.substring(1, quotedId.length() - 1));
            put("groupId", artifact.groupid.getText());
            put("artifactId", artifact.artifactid.getText());
            put("packaging", artifact.packageing.getText());
            put("version", artifact.version.getText());
            if (artifact.scope != null) {
                put("scope", artifact.scope.getText());
            }
        }};
    }
}
