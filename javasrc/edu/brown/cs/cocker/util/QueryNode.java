package edu.brown.cs.cocker.util;

import org.eclipse.jdt.core.dom.ASTNode;

public class QueryNode
{
    ASTNode query_node;
    int query_prop; //-1: no prop; 0: bug; 1: local context; 2: regional context; 3: global context
    boolean query_nested; //Is this a query node nested in another query node?

    public QueryNode(ASTNode node, int prop, boolean nested) {
	this.query_node = node;
	this.query_prop = prop;
	this.query_nested = nested;
    }
    
    public QueryNode(ASTNode node, int prop) {
	this.query_node = node;
	this.query_prop = prop;
	this.query_nested = false;
    }

    public QueryNode(ASTNode node) {
	this.query_node = node;
	this.query_prop = -1;
	this.query_nested = false;
    }

    public ASTNode getNode() { return query_node; }

    public void setNode(ASTNode node) { this.query_node = node; }
    
    public int getProp() { return query_prop; }

    public void setProp(int prop) { this.query_prop = prop; }

    public boolean isNested() { return query_nested; }

    public void setAsNestedOrNot(boolean nested) { this.query_nested = nested; }
}
