package kuona.maven.analyser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

public class PomElement {
    String parentXpath;
    Document element;

    public PomElement(String parentXpath, String xmlText) {

        this.parentXpath = parentXpath;
        try {
            element = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xmlText)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void apply(Document doc) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();

            NodeList nodes = (NodeList) xPath.compile(parentXpath).evaluate(doc, XPathConstants.NODESET);

            if (nodes.getLength() == 0) {
                makePath(doc, parentXpath);
            }
            nodes = (NodeList) xPath.compile(parentXpath).evaluate(doc, XPathConstants.NODESET);

            final Node importNode = doc.importNode(element.getDocumentElement(), true);
            nodes.item(0).appendChild(importNode);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private void makePath(Document doc, String path) {
        try {
            String[] elements = path.split("/");

            String parentPath = String.join("/", Arrays.copyOf(elements, elements.length - 1));

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xPath.compile(parentPath).evaluate(doc, XPathConstants.NODESET);

            if (nodes.getLength() == 0) {
                makePath(doc, parentPath);
            }

            nodes = (NodeList) xPath.compile(parentPath).evaluate(doc, XPathConstants.NODESET);
            nodes.item(0).appendChild(doc.createElement(elements[elements.length - 1]));
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
}
