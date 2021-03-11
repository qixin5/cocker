package edu.brown.cs.cocker.sharpfix;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import edu.brown.cs.cocker.application.ApplicationChunkQuery;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ASTNode;
import edu.brown.cs.cocker.util.*;
import edu.brown.cs.cocker.analysis.*;

public class StmtSearchGlobal
{
    public static void main(String[] args) {
	String query_fpath = args[0];
	String query_stmt_loc = args[1];
	int stmts_lookat_in_method = 2;
	String query_method_loc = getQueryMethodLoc(query_fpath, query_stmt_loc);

	//Invoke cocker to obtain a list of similar methods
	String[] args_global = new String[] { "-a", "kgram3wordmd", "-data", query_method_loc, query_fpath };
	String global_rslt = new ApplicationChunkQuery(args_global).execute();


	//Fetch similar statements in each method
	String global_rslt_in_stmts = getSimilarStmts(global_rslt, stmts_lookat_in_method, query_fpath, query_stmt_loc);


	System.out.println(global_rslt_in_stmts);
    }

    private static String getSimilarStmts(String global_rslt, int stmts_lookat_in_method, String query_fpath, String query_stmt_loc) {
	StringBuilder rslt_sb = null;
	String[] rslt_lines = global_rslt.trim().split("\n");
	StmtIndexComponentGenerator stmt_icg = new StmtIndexComponentGenerator();
	AFGForSharpFixLocalSearch afg = new AFGForSharpFixLocalSearch(5); //The same tokenizer used by the analysis type SHARPFIXLOCAL

	//Load query statement as AST
	ASTNode query_node = ASTNodeFinder.find((CompilationUnit) ASTNodeLoader.getASTNode(new File(query_fpath)), query_stmt_loc).get(0);
	List<String> query_tokens = afg.getTokensInStrings(query_node);
	
	//Scan the global result
	for (String rslt_line : rslt_lines) {
	    rslt_line = rslt_line.trim();
	    if (rslt_line.startsWith("file://")) {
		int first_comma = rslt_line.indexOf(",");
		int last_comma = rslt_line.lastIndexOf(",");
		String fpath = rslt_line.substring(7, first_comma);
		String loc = rslt_line.substring(first_comma+1, last_comma);
		String scorestr = rslt_line.substring(last_comma+1);

		//Load the method as AST
		CompilationUnit cu = (CompilationUnit) ASTNodeLoader.getASTNode(new File(fpath));
		List<ASTNode> md_nodes = ASTNodeFinder.find(cu, loc);
		if (md_nodes == null) { continue; } //Invalid result
		ASTNode md_node = md_nodes.get(0);

		//Visit each inner statement, compare it to query statement, & compute similarity
		List<LocScore> loc_scores = new ArrayList<LocScore>();
		if (md_node instanceof MethodDeclaration) {
		    List<IndexComponent> stmt_ics = stmt_icg.getIndexComponentsForMD((MethodDeclaration) md_node); //Get inner statements in all levels
		    for (IndexComponent stmt_ic : stmt_ics) {
			ASTNode stmt_node = stmt_ic.getNodeList().get(0);

			//Get its loc
			int stmt_startpos = stmt_node.getStartPosition();
			String stmt_loc = "slc:" + cu.getLineNumber(stmt_startpos) + "," + cu.getColumnNumber(stmt_startpos);
			//Get its score
			float stmt_score = getSimilarityScore(query_tokens, afg.getTokensInStrings(stmt_node));

			loc_scores.add(new LocScore(stmt_loc, stmt_score));
		    }
		}

		//Sort stmts by scores
		Collections.sort(loc_scores, new Comparator<LocScore>() {
		    @Override public int compare(LocScore ls0, LocScore ls1) {
			Float f1 = (Float) ls0.getScore();
			Float f2 = (Float) ls1.getScore();
			return f2.compareTo(f1);
		    }
		});

		//Select top-k
		for (int i=0; i<stmts_lookat_in_method && i<loc_scores.size(); i++) {
		    if (rslt_sb == null) { rslt_sb = new StringBuilder(); }
		    else { rslt_sb.append("\n"); }
		    rslt_sb.append("file://"+fpath+","+loc_scores.get(i).getLoc()+","+scorestr);
		}
	    }
	}

	if (rslt_sb != null) { return rslt_sb.toString(); }
	else { return null; }
    }

    private static float getSimilarityScore(List<String> tk_strs0, List<String> tk_strs1) {
	int tk_strs0_size = tk_strs0.size();
        int tk_strs1_size = tk_strs1.size();
        boolean[] matched_arr_b = new boolean[tk_strs1_size];
	int match_count = 0;
        for (int i=0; i<tk_strs0_size; i++) {
            String tk_str0 = tk_strs0.get(i);
            for (int j=0; j<tk_strs1_size; j++) {
                if (matched_arr_b[j]) { continue; }
		String tk_str1 = tk_strs1.get(j);
                if (tk_str0.equals(tk_str1)) {
                    match_count += 1;
                    matched_arr_b[j] = true;
                    break;
                }
            }
	}
        return (2.0f * match_count) / (tk_strs0_size + tk_strs1_size);
    }
    
    private static String getQueryMethodLoc(String query_fpath, String query_stmt_loc) {
	CompilationUnit cu = (CompilationUnit) ASTNodeLoader.getASTNode(new File(query_fpath));
	List<ASTNode> found_nodes = ASTNodeFinder.find(cu, query_stmt_loc);
	if (found_nodes.isEmpty() || found_nodes.get(0)==null) { return null; }

	MethodDeclaration md = null;
	ASTNode curr_node = found_nodes.get(0);
	while (curr_node != null) {
            if (curr_node instanceof MethodDeclaration) {
		md = (MethodDeclaration) curr_node;
		break;
            }
            curr_node = curr_node.getParent();
	}
        if (md == null) { return null; }

	int md_startpos = md.getStartPosition();
	String mdloc = "slc:" + cu.getLineNumber(md_startpos) + "," + cu.getColumnNumber(md_startpos);
        return mdloc;
    }

    private static class LocScore {
	String loc;
	float score;

	public LocScore(String l, float s) {
	    loc = l;
	    score = s;
	}

	public String getLoc() { return loc; }

	public float getScore() { return score; }
    }
}
