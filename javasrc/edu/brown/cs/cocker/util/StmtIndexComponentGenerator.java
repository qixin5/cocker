package edu.brown.cs.cocker.util;

import org.eclipse.jdt.core.dom.*;
import java.util.List;
import java.util.ArrayList;


public class StmtIndexComponentGenerator implements IndexComponentGenerator
{
    //Obtain all statements (wrapped as index components) within the method body
    public List<IndexComponent> getIndexComponentsForMD(MethodDeclaration md) {
	Block md_body_block = md.getBody();
	if (md_body_block == null) {
	    return new ArrayList<IndexComponent>();
	}
	else {
	    ASTNode root = md.getRoot();
	    if (root instanceof CompilationUnit) {
		return getIndexComponentsForStmt((CompilationUnit)root, md_body_block);
	    }
	    else {
		//This shouldn't happen!
		return new ArrayList<IndexComponent>();
	    }
	}
    }

    //index_k is irrelevant and un-unsed
    public List<IndexComponent> getIndexComponentsForMD(MethodDeclaration md, int index_k) {
	return getIndexComponentsForMD(md);
    }
    

    private List<IndexComponent> getIndexComponentsForStmt(CompilationUnit cu, Statement stmt) {
	List<IndexComponent> rslt_list = new ArrayList<IndexComponent>();

	//Add the statement itself (for non-blocks)
	if (!(stmt instanceof Block)) {
	    IndexComponent stmt_ic = new IndexComponent();
	    stmt_ic.addNode(stmt);
	    rslt_list.add(stmt_ic);
	}

	//Recursively add the inner statements
	if (stmt instanceof Block) {
	    List stmt_objs = ((Block) stmt).statements();
	    for (Object stmt_obj : stmt_objs) {
		List<IndexComponent> inner_rslt_list = getIndexComponentsForStmt(cu, (Statement) stmt_obj);
		for (IndexComponent inner_rslt : inner_rslt_list) {
		    rslt_list.add(inner_rslt);
		}
	    }
	}
	
	else if (stmt instanceof DoStatement) {
	    DoStatement do_stmt = (DoStatement) stmt;
	    Statement body_stmt = do_stmt.getBody();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, body_stmt);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	}

	else if (stmt instanceof EnhancedForStatement) {
	    EnhancedForStatement ef_stmt = (EnhancedForStatement) stmt;
	    Statement body_stmt = ef_stmt.getBody();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, body_stmt);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	}

	else if (stmt instanceof ForStatement) {
	    ForStatement for_stmt = (ForStatement) stmt;
	    Statement body_stmt = for_stmt.getBody();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, body_stmt);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	}

	else if (stmt instanceof IfStatement) {
	    IfStatement if_stmt = (IfStatement) stmt;
	    Statement then_stmt = if_stmt.getThenStatement();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, then_stmt);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	    Statement else_stmt = if_stmt.getElseStatement();
	    if (else_stmt != null) {
		List<IndexComponent> iclist3 = getIndexComponentsForStmt(cu, else_stmt);
		for (IndexComponent ic3 : iclist3) {
		    rslt_list.add(ic3);
		}
	    }
	}

	else if (stmt instanceof SwitchStatement) {
	    SwitchStatement ss_stmt = (SwitchStatement) stmt;
	    List stmt_objs = ss_stmt.statements();
	    for (Object stmt_obj : stmt_objs) {
                List<IndexComponent> inner_rslt_list = getIndexComponentsForStmt(cu, (Statement) stmt_obj);
                for (IndexComponent inner_rslt : inner_rslt_list) {
                    rslt_list.add(inner_rslt);
                }
            }
	}
	
	else if (stmt instanceof SynchronizedStatement) {
	    SynchronizedStatement ss_stmt = (SynchronizedStatement) stmt;
	    Statement body_stmt = ss_stmt.getBody();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, body_stmt);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	}

	else if (stmt instanceof TryStatement) {
	    TryStatement try_stmt = (TryStatement) stmt;
	    Statement body_stmt = try_stmt.getBody();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, body_stmt);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }

	    List cc_obj_list = try_stmt.catchClauses();
	    for (Object cc_obj : cc_obj_list) {
		CatchClause cc = (CatchClause) cc_obj;
		List<IndexComponent> iclist3 = getIndexComponentsForStmt(cu, cc.getBody());
		for (IndexComponent ic3 : iclist3) {
		    rslt_list.add(ic3);
		}
	    }

	    Statement finally_stmt = try_stmt.getFinally();
	    if (finally_stmt != null) {
		List<IndexComponent> iclist3 = getIndexComponentsForStmt(cu, finally_stmt);
		for (IndexComponent ic3 : iclist3) {
		    rslt_list.add(ic3);
		}
	    }
	}

	else if (stmt instanceof WhileStatement) {
	    WhileStatement while_stmt = (WhileStatement) stmt;
	    Statement body_stmt = while_stmt.getBody();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, body_stmt);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	}
	else {
	    //Other compound statements are not handled
	}

	return rslt_list;
    }
}
