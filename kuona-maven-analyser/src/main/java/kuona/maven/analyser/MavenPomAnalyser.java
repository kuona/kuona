package kuona.maven.analyser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kuona.maven.analyser.model.FlareNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MavenPomAnalyser {


    public final XPath xPath = XPathFactory.newInstance().newXPath();
    private AnalyserRuntimeOptions runtimeOptions;
    private XPathExpression projectsXpath;
    private XPathExpression dependenciesXpath;
    private XPathExpression groupIdXpath;
    private XPathExpression artifactIdXpath;
    private XPathExpression versionXpath;

    private MavenPomAnalyser() {
        try {
            projectsXpath = xPath.compile("/projects/project");
            dependenciesXpath = xPath.compile("dependencies/dependency");
            groupIdXpath = xPath.compile("groupId/text()");
            artifactIdXpath = xPath.compile("artifactId/text()");
            versionXpath = xPath.compile("version/text()");
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    public MavenPomAnalyser(AnalyserRuntimeOptions runtimeOptions) {
        this();
        this.runtimeOptions = runtimeOptions;
    }

    public static Map<Integer, MavenArtifact> analysePomFile(String includePattern, String artifactPattern, String path) {
        AnalyserRuntimeOptions analyserRuntimeOptions = new AnalyserRuntimeOptions();
        analyserRuntimeOptions.setInclude(includePattern);
        analyserRuntimeOptions.setArtifactFilter(artifactPattern);

        return new MavenPomAnalyser(analyserRuntimeOptions).analysePom(path);
    }

    public static Map<Integer, MavenArtifact> analysePomFile(String path) {
        AnalyserRuntimeOptions analyserRuntimeOptions = new AnalyserRuntimeOptions();

        return new MavenPomAnalyser(analyserRuntimeOptions).analysePom(path);
    }

    public void analyseDependencies() {
        runtimeOptions.getArgs().stream().forEach(this::analysePath);
    }

    private void analysePath(String path) {
        Map<Integer, MavenArtifact> artifacts = analysePom(path);

        render(artifacts);
    }

    private Map<Integer, MavenArtifact> analysePom(String path) {
        Map<Integer, MavenArtifact> artifacts = new HashMap<>();

        final PomFile pomFile = new PomFile();

        try (Closeable ignored = pomFile.inject(path)) {
            new Maven(path).run("mvn help:effective-pom  -Doutput=target/effective-pom.xml", System.out);

            analyseEffectivePom(path + "/target/effective-pom.xml", artifacts);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return artifacts;
    }

    private void render(Map<Integer, MavenArtifact> artifacts) {
        final List<FlareNode> rootLevelFlareNodes = artifacts.values().stream()
                .filter(runtimeOptions.getIncludeFilter())
                .filter(v -> !v.rootLevel())
                .filter(runtimeOptions.getArtifactFilter())
                .map(this::mapMavenArtifact).collect(Collectors.toList());

        final FlareNode rootNode = new FlareNode("project", rootLevelFlareNodes);

        try (final FileWriter writer = new FileWriter("dependencies.json")) {

            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(rootNode));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private FlareNode mapMavenArtifact(MavenArtifact a) {

        final FlareNode result = new FlareNode(a.getArtifactId());
        a.getDependencies().stream().filter(runtimeOptions.getIncludeFilter()).forEach(d -> result.add(mapMavenArtifact(d)));
        return result;
    }

    private void analyseEffectivePom(String path, Map<Integer, MavenArtifact> artifacts) {

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            final Document pom = db.parse(new File(path));

            NodeList nodes = (NodeList) projectsXpath.evaluate(pom, XPathConstants.NODESET);

            int nodecount = nodes.getLength();
            for (int i = 0; i < nodecount; i++) {
                // Removing the node from the DOM speeds up processing SIGNIFICANTLY
                final Node node = nodes.item(i);
                node.getParentNode().removeChild(node);
                analyseProject(node, artifacts);

            }


        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }

    }

    private void analyseProject(Node projectNode, Map<Integer, MavenArtifact> artifacts) {
        try {

            MavenArtifact mavenArtifact = new MavenArtifact((String) groupIdXpath.evaluate(projectNode, XPathConstants.STRING), (String) artifactIdXpath.evaluate(projectNode, XPathConstants.STRING), (String) versionXpath.evaluate(projectNode, XPathConstants.STRING));

            if (!artifacts.containsKey(mavenArtifact.hashCode())) {
                artifacts.put(mavenArtifact.hashCode(), mavenArtifact);
            } else {
                mavenArtifact = artifacts.get(mavenArtifact.hashCode());
            }

            System.out.println("Found " + mavenArtifact);

            NodeList nodes = (NodeList) dependenciesXpath.evaluate(projectNode, XPathConstants.NODESET);

            int nodecount = nodes.getLength();
            for (int i = 0; i < nodecount; i++) {
                MavenArtifact artifact = new MavenArtifact((String) groupIdXpath.evaluate(nodes.item(i), XPathConstants.STRING), (String) artifactIdXpath.evaluate(nodes.item(i), XPathConstants.STRING), (String) versionXpath.evaluate(nodes.item(i), XPathConstants.STRING));

                if (!artifacts.containsKey(artifact.hashCode())) {
                    artifacts.put(artifact.hashCode(), artifact);
                } else {
                    artifact = artifacts.get(artifact.hashCode());
                }

                mavenArtifact.addDependent(artifact);
            }

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }


    }

}
