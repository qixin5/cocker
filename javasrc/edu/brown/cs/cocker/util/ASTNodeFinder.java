package edu.brown.cs.cocker.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.brown.cs.ivy.file.IvyLog;

import org.eclipse.jdt.core.dom.ASTVisitor;
import java.util.List;
import java.util.ArrayList;

public class ASTNodeFinder
{
    @SuppressWarnings("unused")
    public static List<ASTNode> find(CompilationUnit cu, String loc) {
       List<ASTNode> rslt_list = new ArrayList<ASTNode>();
       if (cu == null) { return rslt_list; }
       String[] sublocs = loc.split(";");
       for (String subloc : sublocs) {
          String[] subloc_items = subloc.split(":");
          String subloc_title = "", subloc_ctnt = "";
          try {
             subloc_title = subloc_items[0];
             subloc_ctnt = subloc_items[1];
           } 
          catch (Exception e) {
             IvyLog.logE("UTIL","Unknown loc: " + subloc);
             return new ArrayList<ASTNode>();
           }
          ASTNode found_node = findNode(cu, subloc_ctnt);
          rslt_list.add(found_node);
	}
       return rslt_list;
    }

    private static ASTNode findNode(CompilationUnit cu, String loc_ctnt) {
	String[] rc = loc_ctnt.split(",");
	int sln = -1, scn = -1;
	try {
	    sln = Integer.parseInt(rc[0]); //start line number
	    scn = Integer.parseInt(rc[1]); //start column number
	}
        catch (Exception e) {
	    IvyLog.logE("UTIL","Parsing Error: " + loc_ctnt);
	}
	if (sln == -1 || scn == -1) { return null; }
	int charseq = cu.getPosition(sln, scn);
	NodeFindVisitor nfv = new NodeFindVisitor(charseq);
	cu.accept(nfv);
	return nfv.getFoundNode();
    }

    private static class NodeFindVisitor extends ASTVisitor
    {
	private int char_seq;
	private ASTNode found_node;

	public NodeFindVisitor(int charseq) { this.char_seq = charseq; }

	public ASTNode getFoundNode() { return found_node; }

	@Override public boolean preVisit2(ASTNode node) {
	    int node_charseq = node.getStartPosition();
	    if (node_charseq == char_seq) {
		found_node = node;
		return false;
	    }
	    else if (node_charseq > char_seq) {
		return false; //unlikely to find the target
	    }
	    else {
		return true; //Still need to visit the children
	    }
	}
    }
}
