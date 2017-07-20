package kuona.maven.analyser.model;

import java.util.ArrayList;
import java.util.List;

public class FlareNode {
    String name;
    List<FlareNode> children;

    public FlareNode(String name) {
        this(name, new ArrayList<>());
    }

    public FlareNode(String name, List<FlareNode> children) {
        this.name = name;
        this.children = children;
    }

    public void add(FlareNode node) {
        children.add(node);
    }
}
