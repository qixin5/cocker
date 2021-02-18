package edu.brown.cs.cocker.analysis;

import java.util.List;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import edu.brown.cs.cocker.util.*;

/* AFG Code Item 0 */
public class AFGCI0 extends AFG
{
    private CodeItemExtractor cie;
    
    public AFGCI0(Object k0) {
	super(k0);
	cie = new CodeItemExtractor();
    }

    @Override public List<PatternToken> getTokens(ASTNode node) {
	List<ASTNode> node_list = new ArrayList<ASTNode>();
	node_list.add(node);
	return getTokensHelper(node_list);
    }

    /* For indexing, node_list is passed from the Reader object. See AnalysisJavaTokenizer.java. */
    @Override public List<PatternToken> getTokens(List<ASTNode> node_list) {
	return getTokensHelper(node_list);
    }

    /* For query, node & data are passed from the query input. */
    @Override public List<PatternToken> getTokens(ASTNode node,String data) {
	if (!(node instanceof CompilationUnit)) { return this.getTokens(node); }
	CompilationUnit cu = (CompilationUnit) node;
	List<QueryNode> qnode_list = QueryNodeFinder.find(cu, data);
	List<ASTNode> node_list = new ArrayList<ASTNode>();
	for (QueryNode qnode : qnode_list) { node_list.add(qnode.getNode()); }
	return getTokensHelper(node_list);
    }

    @Override public List<PatternToken> getTokens(String cnts) {
	return new ArrayList<PatternToken>();
    }

    /* Properties 
     0: class name
     1: method name
     2: parameter type name
     3: parameter name
     4: method call name
     5: type name
     6: var name */
    private List<PatternToken> getTokensHelper(List<ASTNode> node_list) {
	CodeItem ci = cie.extract(node_list);
	String cname = ci.getClassName();
	String mname = ci.getMethodName();
	List<String> param_type_names = ci.getParameterTypeNames();	
	List<String> param_names = ci.getParameterNames();
	List<String> method_call_names = ci.getMethodCallNames();
	List<String> type_names = ci.getTypeNames();
	List<String> var_names = ci.getVariableNames();

	List<PatternToken> pt_list = new ArrayList<PatternToken>();
	int pos = 0;
	List<String> ci_words = null;

	if (cname != null) {
	    ci_words = AnalysisCodeWordGenerator.stem(cname);
	    for (String ci_word : ci_words) {
		PatternToken pt = new CodeItemWord(ci_word, pos);
		pos += 1;
		pt.setProp(0);
		pt_list.add(pt);
	    }
	}

	if (mname != null) {
	    ci_words = AnalysisCodeWordGenerator.stem(mname);
	    for (String ci_word : ci_words) {
		PatternToken pt = new CodeItemWord(ci_word, pos);
		pos += 1;
		pt.setProp(1);
		pt_list.add(pt);
	    }
	}

	if (param_type_names != null) {
	    for (String param_type_name : param_type_names) {
		ci_words = AnalysisCodeWordGenerator.stem(param_type_name);
		for (String ci_word : ci_words) {
		    PatternToken pt = new CodeItemWord(ci_word, pos);
		    pos += 1;
		    pt.setProp(2);
		    pt_list.add(pt);
		}
	    }
	}
	
	if (param_names != null) {
	    for (String param_name : param_names) {
		ci_words = AnalysisCodeWordGenerator.stem(param_name);
		for (String ci_word : ci_words) {
		    PatternToken pt = new CodeItemWord(ci_word, pos);
		    pos += 1;
		    pt.setProp(3);
		    pt_list.add(pt);
		}
	    }
	}

	if (method_call_names != null) {
	    for (String method_call_name : method_call_names) {
		ci_words = AnalysisCodeWordGenerator.stem(method_call_name);
		for (String ci_word : ci_words) {
		    PatternToken pt = new CodeItemWord(ci_word, pos);
		    pos += 1;
		    pt.setProp(4);
		    pt_list.add(pt);
		}
	    }
	}

	if (type_names != null) {
	    for (String type_name : type_names) {
		ci_words = AnalysisCodeWordGenerator.stem(type_name);
		for (String ci_word : ci_words) {
		    PatternToken pt = new CodeItemWord(ci_word, pos);
		    pos += 1;
		    pt.setProp(5);
		    pt_list.add(pt);
		}
	    }
	}

	if (var_names != null) {
	    for (String var_name : var_names) {
		ci_words = AnalysisCodeWordGenerator.stem(var_name);
		for (String ci_word : ci_words) {
		    PatternToken pt = new CodeItemWord(ci_word, pos);
		    pos += 1;
		    pt.setProp(6);
		    pt_list.add(pt);
		}
	    }
	}

	return pt_list;
	/*
	String ci_str = getCodeItemString(ci);
	List<String> ci_words = AnalysisCodeWordGenerator.stem(ci_str);
	List<PatternToken> pt_list = new ArrayList<PatternToken>();
	int pos = 0;
	for (String ci_word : ci_words) {
	    PatternToken pt = new CodeItemWord(ci_word, pos);
	    pos += 1;
	    pt_list.add(pt);
	}
	return pt_list;
	*/
    }
    
    @SuppressWarnings("unused")
    private List<PatternToken> mergePatternTokenLists(List<PatternToken> pt_list1, List<PatternToken> pt_list2) {
        int curr_pt_pos = pt_list1.size();
        for (PatternToken pt2 : pt_list2) {
            pt2.setPosition(curr_pt_pos); //Reset the position
            curr_pt_pos++;
            pt_list1.add(pt2);
        }
        return pt_list1;
    }

    @SuppressWarnings("unused")
    private String getCodeItemString(CodeItem ci) {
        if (ci == null) { return ""; }
        StringBuilder sb = new StringBuilder();
        String cname = ci.getClassName();
        String mname = ci.getMethodName();
        List<String> param_names = ci.getParameterNames();
        List<String> param_type_names = ci.getParameterTypeNames();
        List<String> method_call_names = ci.getMethodCallNames();
        List<String> type_names = ci.getTypeNames();
        List<String> var_names = ci.getVariableNames();
    
        if (cname != null) { sb.append(cname); }
        if (mname != null) { sb.append(" " + mname); }
        if (param_names != null) {
            for (String param_name : param_names) {
        	sb.append(" " + param_name);
            }
        }
        if (param_type_names != null) {
            for (String param_type_name : param_type_names) {
        	sb.append(" " + param_type_name);
            }
        }
        if (method_call_names != null) {
            for (String method_call_name : method_call_names) {
        	sb.append(" " + method_call_name);
            }
        }
        if (type_names != null) {
            for (String type_name : type_names) {
        	sb.append(" " + type_name);
            }
        }
        if (var_names != null) {
            for (String var_name : var_names) {
        	sb.append(" " + var_name);
            }
        }
        return sb.toString();
    }

    static class CodeItemWord implements PatternToken {

	private String code_text;
	private int    code_pos;
	private int    code_prop;

	CodeItemWord(String text, int pos) {
	    this.code_text = text;
	    this.code_pos = pos;
	    code_prop = -1;
	}

	@Override public String getText() {
	    return code_text;
	}

	@Override public int getPosition() {
	    return code_pos;
	}

	@Override public void setPosition(int pos) {
	    this.code_pos = pos;
	}

	@Override public String toString() {
	    return code_text + "@" + code_pos;
	}

	@Override public int getProp() {
	    return code_prop;
	}

	@Override public void setProp(int prop) {
	    this.code_prop = prop;
	}
    }
}
