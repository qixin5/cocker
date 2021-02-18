package edu.brown.cs.cocker.util;

import org.eclipse.jdt.core.dom.*;
import java.util.List;
import java.util.ArrayList;


public class SSFIXIndexComponentGenerator implements IndexComponentGenerator
{
    public List<IndexComponent> getIndexComponentsForMD(MethodDeclaration md) {
	return getIndexComponentsForMD(md, 3);
    }
    
    public List<IndexComponent> getIndexComponentsForMD(MethodDeclaration md, int index_k) {
	Block md_body_block = md.getBody();
	if (md_body_block == null) {
	    return new ArrayList<IndexComponent>();
	}
	else {
	    ASTNode root = md.getRoot();
	    if (root instanceof CompilationUnit) {
		return getIndexComponentsForBlock((CompilationUnit)root, md_body_block, index_k);
	    }
	    else {
		//This shouldn't happen!
		return new ArrayList<IndexComponent>();
	    }
	}
    }
    
    private List<IndexComponent> getIndexComponentsForStmtObjList(CompilationUnit cu, List<?> stmt_obj_list, int index_k) {
	List<IndexComponent> rslt_list = new ArrayList<IndexComponent>();
	int stmt_list_size = stmt_obj_list.size();
	if (stmt_list_size == 0) { return rslt_list; }
	else if (stmt_list_size == 1) {
	    Statement stmt = (Statement) stmt_obj_list.get(0);
	    if (!isCompoundStmt(stmt)) { //Avoid adding a compound stmt twice later
		IndexComponent ic = new IndexComponent();
		ic.addNode(stmt);
		rslt_list.add(ic);
	    }
	}
	//Add the sequential stmts
	else if (stmt_list_size <= index_k) {
	    IndexComponent ic = new IndexComponent();
	    for (int i=0; i<stmt_list_size; i++) {
		ic.addNode((Statement) stmt_obj_list.get(i));
	    }
	    rslt_list.add(ic);
	}
	//Add every k-sequential stmts	
	else {
	    for (int i=0; i<=stmt_list_size-index_k; i++) {
		IndexComponent ic = new IndexComponent();
		for (int j=i; j<i+index_k; j++) {
		    ic.addNode((Statement) stmt_obj_list.get(j));
		}
		rslt_list.add(ic);
	    }
	}
	//Recursive collection for each compound stmt
	for (int i=0; i<stmt_list_size; i++) {
	    Statement stmt = (Statement) stmt_obj_list.get(i);
	    if ((stmt instanceof Block) || isCompoundStmt(stmt)) {
		List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, stmt, index_k);
		for (IndexComponent ic2 : iclist2) {
		    rslt_list.add(ic2);
		}
	    }
	}
	return rslt_list;
    }

    private List<IndexComponent> getIndexComponentsForBlock(CompilationUnit cu, Block block, int index_k) {
	List<?> stmt_obj_list = block.statements();
	return getIndexComponentsForStmtObjList(cu, stmt_obj_list, index_k);
    }
    
    private List<IndexComponent> getIndexComponentsForStmt(CompilationUnit cu, Statement stmt, int index_k) {
	if (stmt instanceof Block) { //Do not add the block itself
	    return getIndexComponentsForBlock(cu, (Block) stmt, index_k);
	}

	List<IndexComponent> rslt_list = new ArrayList<IndexComponent>();
	IndexComponent stmt_ic = new IndexComponent();
	stmt_ic.addNode(stmt);
	rslt_list.add(stmt_ic);
	
	if (stmt instanceof DoStatement) {
	    DoStatement do_stmt = (DoStatement) stmt;	    
	    Expression cond_expr = do_stmt.getExpression();
	    if (cond_expr != null) {
		IndexComponent cond_ic = new IndexComponent();
		cond_ic.addNode(cond_expr);
		rslt_list.add(cond_ic);
	    }
	    Statement body_stmt = do_stmt.getBody();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, body_stmt, index_k);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	}
	else if (stmt instanceof EnhancedForStatement) {
	    EnhancedForStatement ef_stmt = (EnhancedForStatement) stmt;
	    Statement body_stmt = ef_stmt.getBody();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, body_stmt, index_k);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	}
	else if (stmt instanceof ForStatement) {
	    ForStatement for_stmt = (ForStatement) stmt;
	    Expression cond_expr = for_stmt.getExpression();
	    if (cond_expr != null) {
		IndexComponent cond_ic = new IndexComponent();
		cond_ic.addNode(cond_expr);
		rslt_list.add(cond_ic);
	    }
	    Statement body_stmt = for_stmt.getBody();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, body_stmt, index_k);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	}
	else if (stmt instanceof IfStatement) {
	    IfStatement if_stmt = (IfStatement) stmt;
	    Expression cond_expr = if_stmt.getExpression();
	    if (cond_expr != null) {
		IndexComponent cond_ic = new IndexComponent();
		cond_ic.addNode(cond_expr);
		rslt_list.add(cond_ic);
	    }
	    Statement then_stmt = if_stmt.getThenStatement();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, then_stmt, index_k);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	    Statement else_stmt = if_stmt.getElseStatement();
	    if (else_stmt != null) {
		List<IndexComponent> iclist3 = getIndexComponentsForStmt(cu, else_stmt, index_k);
		for (IndexComponent ic3 : iclist3) {
		    rslt_list.add(ic3);
		}
	    }
	}
	else if (stmt instanceof SwitchStatement) {
	    SwitchStatement ss_stmt = (SwitchStatement) stmt;
	    List<?> stmt_obj_list = ss_stmt.statements();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmtObjList(cu, stmt_obj_list, index_k);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	}
	else if (stmt instanceof SynchronizedStatement) {
	    SynchronizedStatement ss_stmt = (SynchronizedStatement) stmt;
	    Statement body_stmt = ss_stmt.getBody();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, body_stmt, index_k);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	}
	else if (stmt instanceof TryStatement) {
	    TryStatement try_stmt = (TryStatement) stmt;
	    Statement body_stmt = try_stmt.getBody();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, body_stmt, index_k);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	    List<?> cc_obj_list = try_stmt.catchClauses();
	    for (Object cc_obj : cc_obj_list) {
		CatchClause cc = (CatchClause) cc_obj;
		List<IndexComponent> iclist3 = getIndexComponentsForStmt(cu, cc.getBody(), index_k);
		for (IndexComponent ic3 : iclist3) {
		    rslt_list.add(ic3);
		}
	    }
	    Statement finally_stmt = try_stmt.getFinally();
	    if (finally_stmt != null) {
		List<IndexComponent> iclist3 = getIndexComponentsForStmt(cu, finally_stmt, index_k);
		for (IndexComponent ic3 : iclist3) {
		    rslt_list.add(ic3);
		}
	    }
	}
	else if (stmt instanceof WhileStatement) {
	    WhileStatement while_stmt = (WhileStatement) stmt;
	    Expression cond_expr = while_stmt.getExpression();
	    if (cond_expr != null) {
		IndexComponent cond_ic = new IndexComponent();
		cond_ic.addNode(cond_expr);
		rslt_list.add(cond_ic);
	    }
	    Statement body_stmt = while_stmt.getBody();
	    List<IndexComponent> iclist2 = getIndexComponentsForStmt(cu, body_stmt, index_k);
	    for (IndexComponent ic2 : iclist2) {
		rslt_list.add(ic2);
	    }
	}
	else {
	    //Do nothing more for other statements
	}
	return rslt_list;
    }
    
    private boolean isCompoundStmt(Statement stmt) {
	return ((stmt instanceof DoStatement) || (stmt instanceof EnhancedForStatement)
		|| (stmt instanceof ForStatement) || (stmt instanceof IfStatement)
		|| (stmt instanceof SwitchStatement) || (stmt instanceof SynchronizedStatement)
		|| (stmt instanceof TryStatement) || (stmt instanceof WhileStatement));
    }
}
