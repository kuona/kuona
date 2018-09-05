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
      System.out.println("Injecting " + parentXpath);
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

  private NodeList nodes(Document doc, String path) {
    XPath xPath = XPathFactory.newInstance().newXPath();
    try {
      return (NodeList) xPath.compile(path).evaluate(doc, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }

  }

  private void makePath(Document doc, String path) {
    String[] elements = path.split("/");

    String parentPath = String.join("/", Arrays.copyOf(elements, elements.length - 1));

    NodeList nodes = nodes(doc, parentPath);

    if (nodes.getLength() == 0) {
      makePath(doc, parentPath);
    }

    nodes = nodes(doc, parentPath);
    String tag = elements[elements.length - 1];
    nodes.item(0).appendChild(doc.createElement(tag));

    System.out.println("Adding " + tag + " to " + parentPath);
  }
}
