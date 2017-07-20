package kuona.maven.analyser;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PomFile {
    public static final String REPORT_ELEMENT = "<plugin>\n" +
            "    <groupId>org.apache.maven.plugins</groupId>\n" +
            "    <artifactId>maven-dependency-plugin</artifactId>\n" +
            "    <version>2.10</version>\n" +
            "</plugin>\n";
    public static final String PLUGIN_REPORT_ELEMENT = "<plugins>" + REPORT_ELEMENT + "</plugins>";
    public static final String REPORTING_ELEMENT = "<reporting>" + PLUGIN_REPORT_ELEMENT + "</reporting>";
    public static final String PLUGIN_ELEMENT = "<plugin>\n" +
            "    <groupId>org.apache.maven.plugins</groupId>\n" +
            "    <artifactId>maven-dependency-plugin</artifactId>\n" +
            "    <version>2.10</version>\n" +
            "    <executions>\n" +
            "        <execution>\n" +
            "            <id>analyze</id>\n" +
            "            <goals>\n" +
            "                <goal>analyze</goal>\n" +
            "            </goals>\n" +
            "            <configuration>\n" +
            "                <failOnWarning>true</failOnWarning>\n" +
            "                <outputXML>true</outputXML>\n" +
            "            </configuration>\n" +
            "        </execution>\n" +
            "    </executions>\n" +
            "</plugin>\n";
    public static final String DEPENDENCY_ELEMENT = "<dependency>\n" +
            "    <groupId>org.apache.maven.plugins</groupId>\n" +
            "    <artifactId>maven-dependency-plugin</artifactId>\n" +
            "    <version>2.10</version>\n" +
            "    <type>maven-plugin</type>\n" +
            "</dependency>\n";


    public static final String HELP_PLUGIN_ELEMENT =
            "<plugin>\n" +
                    "    <groupId>org.apache.maven.plugins</groupId>\n" +
                    "    <artifactId>maven-help-plugin</artifactId>\n" +
                    "    <version>2.2</version>\n" +
                    "</plugin>\n";
    public static final String HELP_PLUGIN_ELEMENT2 =

            "<plugin>\n" +
                    "    <groupId>org.apache.maven.plugins</groupId>\n" +
                    "    <artifactId>maven-help-plugin</artifactId>\n" +
                    "    <version>2.2</version>\n" +
                    "</plugin>\n";

    private final List<PomElement> dependencies;

    public PomFile() {
        dependencies = new ArrayList<>();
        dependencies.add(new PomElement("/project/dependencyManagement/dependencies/.", DEPENDENCY_ELEMENT));
        dependencies.add(new PomElement("/project/build/pluginManagement/plugins/.", PLUGIN_ELEMENT));
        dependencies.add(new PomElement("/project/reporting/plugins/.", REPORT_ELEMENT));
        dependencies.add(new PomElement("/project/reporting/.", PLUGIN_REPORT_ELEMENT));
        dependencies.add(new PomElement("/project", REPORTING_ELEMENT));
        dependencies.add(new PomElement("/project/build/pluginManagement/plugins", HELP_PLUGIN_ELEMENT));
        dependencies.add(new PomElement("/project/build/plugins", HELP_PLUGIN_ELEMENT2));

    }


    public Closeable inject(String path) {
        File pomFile = new File(path + "/pom.xml");
        final File originalPomFile = new File(path + "pom.xml.original");
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            if (pomFile.exists()) {
                if (pomFile.renameTo(originalPomFile)) {
                    Document doc = db.parse(originalPomFile);

                    dependencies.stream().forEach(d -> d.apply(doc));
                    DOMSource source = new DOMSource(doc);

                    final FileOutputStream outputStream = new FileOutputStream(pomFile);
                    StreamResult result = new StreamResult(outputStream);

                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.transform(source, result);
                    outputStream.close();

                    return () -> {
                        pomFile.delete();
                        originalPomFile.renameTo(pomFile);
                    };
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            throw new RuntimeException(e);
        }

        return () -> {
        };
    }
}
