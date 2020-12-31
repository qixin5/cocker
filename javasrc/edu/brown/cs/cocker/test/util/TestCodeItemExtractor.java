package edu.brown.cs.cocker.util;

import java.util.List;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
//import asts.*;
import java.io.*;

public class TestCodeItemExtractor
{
    public static void main(String[] args) {
	String fpath = args[0];
	String loc = args[1];
	File f = new File(fpath);
	ASTNode root = ASTNodeLoader.getResolvedASTNode(f);
	CompilationUnit cu = (CompilationUnit) root;
	List<ASTNode> node_list = ASTNodeFinder.find(cu, loc);
	CodeItemExtractor cie = new CodeItemExtractor();
	CodeItem ci = cie.extract(node_list);

	StringBuilder sb = new StringBuilder();
	sb.append("Class Name: " + ci.getClassName()); sb.append("\n");
	sb.append("Method Name: " + ci.getMethodName()); sb.append("\n");
	sb.append("Parameter Type Names: "); sb.append("\n");
	List<String> param_type_names = ci.getParameterTypeNames();
	for (String param_type_name : param_type_names) {
	    sb.append(param_type_name);
	    sb.append(" ");
	}
	sb.append("\n");
	sb.append("Parameter Names: "); sb.append("\n");
	List<String> param_names = ci.getParameterNames();
	for (String param_name : param_names) {
	    sb.append(param_name);
	    sb.append(" ");
	}
	sb.append("\n");	
	sb.append("Method Call Names: "); sb.append("\n");
	List<String> mc_names = ci.getMethodCallNames();
	for (String mc_name : mc_names) {
	    sb.append(mc_name);
	    sb.append(" ");
	}
	sb.append("\n");	
	sb.append("Type Names: "); sb.append("\n");
	List<String> type_names = ci.getTypeNames();
	for (String type_name : type_names) {
	    sb.append(type_name);
	    sb.append(" ");
	}
	sb.append("\n");
	sb.append("Variable Names: "); sb.append("\n");
	List<String> var_names = ci.getVariableNames();
	for (String var_name : var_names) {
	    sb.append(var_name);
	    sb.append(" ");
	}

	System.out.println(sb.toString());
    }
}
