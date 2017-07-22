package kuona.maven.analyser;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PomElementTest {
    @Test
    public void addsElementToRoot() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project></project>")));
        final PomElement pomElement = new PomElement("/project", "<child/>");

        pomElement.apply(document);

        assertThat(documentToString(document), is("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><project><child/></project>"));

    }

    @Test
    public void addsPathIfMissing() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project></project>")));
        final PomElement pomElement = new PomElement("/project/path/to", "<child/>");

        pomElement.apply(document);

        assertThat(documentToString(document), is("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><project><path><to><child/></to></path></project>"));

    }

    private String documentToString(Document document) throws TransformerException, IOException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(outputStream);
        DOMSource source = new DOMSource(document);
        transformer.transform(source, result);
        outputStream.close();

        return new String(outputStream.toByteArray());
    }
}
