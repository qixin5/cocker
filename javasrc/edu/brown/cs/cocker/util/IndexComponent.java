package edu.brown.cs.cocker.util;

import java.util.List;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.ASTNode;

public class IndexComponent
{
    List<ASTNode> node_list;

    public IndexComponent() {
	node_list = new ArrayList<ASTNode>();
    }

    public IndexComponent(List<ASTNode> nodelist) {
        this.node_list = nodelist;
    }

    public void addNode(ASTNode node) { node_list.add(node); }

    public List<ASTNode> getNodeList() { return node_list; }
}
