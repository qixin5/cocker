package edu.brown.cs.cocker.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;


public class QueryNodeFinder
{
    private final static int BUG_PROP = 0;
    private final static int LOCAL_CTXT_PROP = 1;
    private final static int REGIONAL_CTXT_PROP = 2;
    private final static int GLOBAL_CTXT_PROP = 3;
    
    public static List<QueryNode> find(CompilationUnit cu, String data) {

	List<QueryNode> found_qnode_list = new ArrayList<QueryNode>();
	if (cu == null) { return found_qnode_list; }

	String[] data_items = data.split(";");
	for (String data_item : data_items) {
	    String[] data_subitems = data_item.split(":");
	    String title = "", loc = "";
	    try {
		title = data_subitems[0];
		loc = data_subitems[1];
	    } catch (Exception e) {
		System.err.println("Parsing Error: " + data_item);
		e.printStackTrace();
		return found_qnode_list;
	    }

	    List<ASTNode> found_node_list0 = findBySLC(cu, loc);
	    for (ASTNode found_node0 : found_node_list0) {
		QueryNode qnode0 = null;
		if ("bslc".equals(title)) {
		    qnode0 = new QueryNode(found_node0, BUG_PROP);
		}
		else if ("bslc-nested".equals(title)) {
		    qnode0 = new QueryNode(found_node0, BUG_PROP, true);
		}
		else if ("lcslc".equals(title) || "cslc".equals(title) || "slc".equals(title)) {
		    qnode0 = new QueryNode(found_node0, LOCAL_CTXT_PROP);
		}
		else if ("lcslc-nested".equals(title) || "cslc-nested".equals(title) || "slc-nested".equals(title)) {
		    qnode0 = new QueryNode(found_node0, LOCAL_CTXT_PROP, true);
		}
		else if ("rcslc".equals(title)) {
		    qnode0 = new QueryNode(found_node0, REGIONAL_CTXT_PROP);
		}
		else if ("rcslc-nested".equals(title)) {
		    qnode0 = new QueryNode(found_node0, REGIONAL_CTXT_PROP, true);
		}
		else if ("gcslc".equals(title)) {
		    qnode0 = new QueryNode(found_node0, GLOBAL_CTXT_PROP);
		}
		else if ("gcslc-nested".equals(title)) {
		    qnode0 = new QueryNode(found_node0, GLOBAL_CTXT_PROP, true);
		}
		if (qnode0 != null) {
		    found_qnode_list.add(qnode0);
		}
		else {
		    System.err.println("Unknown data: " + data);
		    return found_qnode_list;
		}
	    }
	}

	return found_qnode_list;
    }

    private static List<ASTNode> findBySLC(CompilationUnit cu, String loc_str) {
    
        //Create a list of charseq numbers
        List<Integer> charseq_list = new ArrayList<Integer>();
        String[] loc_items = loc_str.split(",");
        int start_ln = -1, start_cn = -1;
        try {
            start_ln = Integer.parseInt(loc_items[0]);
            start_cn = Integer.parseInt(loc_items[1]);
        } catch (Exception e) {
            System.err.println("Parsing Error for " + loc_str);
            return new ArrayList<ASTNode>();
        }
    
        //Get the character sequence number
        int charseq = cu.getPosition(start_ln, start_cn);
        charseq_list.add(charseq);
    
        //Traversal over the compilation unit
        FindVisitor fv = new FindVisitor(charseq_list);
        cu.accept(fv);
    
        //Put results into a list
        List<ASTNode> tnode_list = fv.getResult();
    
        return tnode_list;
    }

    private static class FindVisitor extends ASTVisitor
    {
	Set<Integer> charseq_set;
	List<ASTNode> target_list;

	public FindVisitor(List<Integer> charseq_list) {

	    charseq_set = new HashSet<Integer>();
	    for (Integer charseq_int_obj : charseq_list) {
		charseq_set.add(charseq_int_obj.intValue());
	    }
	    target_list = new ArrayList<ASTNode>();
	}

	public List<ASTNode> getResult() { return target_list; }

	//Visiting method
	@Override public void preVisit(ASTNode node) {

	    int pos = node.getStartPosition();
	    if (charseq_set.contains(pos)) {
		target_list.add(node);
		charseq_set.remove(pos);
	    }
	}
    }
}
