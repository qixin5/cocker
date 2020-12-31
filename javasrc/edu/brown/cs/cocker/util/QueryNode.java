package edu.brown.cs.cocker.util;

import org.eclipse.jdt.core.dom.ASTNode;

public class QueryNode
{
    ASTNode node;
    int prop; //-1: no prop; 0: bug; 1: local context; 2: regional context; 3: global context
    boolean nested; //Is this a query node nested in another query node?

    public QueryNode(ASTNode node, int prop, boolean nested) {
	this.node = node;
	this.prop = prop;
	this.nested = nested;
    }
    
    public QueryNode(ASTNode node, int prop) {
	this.node = node;
	this.prop = prop;
	this.nested = false;
    }

    public QueryNode(ASTNode node) {
	this.node = node;
	this.prop = -1;
	this.nested = false;
    }

    public ASTNode getNode() { return node; }

    public void setNode(ASTNode node) { this.node = node; }
    
    public int getProp() { return prop; }

    public void setProp(int prop) { this.prop = prop; }

    public boolean isNested() { return nested; }

    public void setAsNestedOrNot(boolean nested) { this.nested = nested; }
}
