package edu.brown.cs.cocker.util;

import org.eclipse.jdt.core.dom.*;
import java.util.List;
import java.util.ArrayList;


public class GeneralIndexComponentGenerator implements IndexComponentGenerator
{
    public List<IndexComponent> getIndexComponentsForMD(MethodDeclaration md) {
	return getIndexComponentsForMD(md, 3);
    }
    
    public List<IndexComponent> getIndexComponentsForMD(MethodDeclaration md, int index_k) {
	Block md_body_block = md.getBody();
	if (md_body_block == null) { return new ArrayList<IndexComponent>(); }
	ASTNode root = md.getRoot();
	if (root instanceof CompilationUnit) {
	    return getIndexComponentsForBlock((CompilationUnit)root, md_body_block, index_k);
	}
	else {
	    //This shouldn't happen!
	    return new ArrayList<IndexComponent>();
	}
    }

    protected List<IndexComponent> getIndexComponentsForBlock(CompilationUnit cu, Block block, int index_k) {
        List<?> stmt_obj_list = block.statements();
        return getIndexComponentsForStmtObjList(cu, stmt_obj_list, index_k);
    }
    
    protected List<IndexComponent> getIndexComponentsForStmtObjList(CompilationUnit cu, List<?> stmt_obj_list, int index_k) {
        List<IndexComponent> rslt_list = new ArrayList<IndexComponent>();
        int stmt_list_size = stmt_obj_list.size();
        if (stmt_list_size == 0) { return rslt_list; }
    
        //Add if there are less-than-index_k stmt, simply add all the stmts
        else if (stmt_list_size <= index_k) {
            IndexComponent ic = new IndexComponent();
            for (int i=0; i<stmt_list_size; i++) {
        	ic.addNode((Statement) stmt_obj_list.get(i));
            }
            rslt_list.add(ic);
        }
    
        //Otherwise, add every k-sequential stmts
        else {
            for (int i=0; i<=stmt_list_size-index_k; i++) {
        	IndexComponent ic = new IndexComponent();
        	for (int j=i; j<i+index_k; j++) {
        	    ic.addNode((Statement) stmt_obj_list.get(j));
        	}
        	rslt_list.add(ic);
            }
        }
        
        //Add index components from every block or
        //the body block of every compound stmt (not inclusive)
        for (int i=0; i<stmt_list_size; i++) {
            Statement stmt = (Statement) stmt_obj_list.get(i);
    
            if (stmt instanceof Block) {
        	List<IndexComponent> stmt_ic_list = getIndexComponentsForBlock(cu, ((Block) stmt), index_k);
        	for (IndexComponent stmt_ic : stmt_ic_list) {
        	    rslt_list.add(stmt_ic);
        	}
            }
    
            else if (stmt instanceof DoStatement) {
        	Statement bodystmt = ((DoStatement) stmt).getBody();
        	List<IndexComponent> bodystmt_ic_list = getIndexComponentsFromBodyStmt(cu, bodystmt, index_k);
        	for (IndexComponent bodystmt_ic : bodystmt_ic_list) {
        	    rslt_list.add(bodystmt_ic);
        	}
            }
            else if (stmt instanceof EnhancedForStatement) {
        	Statement bodystmt = ((EnhancedForStatement) stmt).getBody();
        	List<IndexComponent> bodystmt_ic_list = getIndexComponentsFromBodyStmt(cu, bodystmt, index_k);
        	for (IndexComponent bodystmt_ic : bodystmt_ic_list) {
        	    rslt_list.add(bodystmt_ic);
        	}
            }
            else if (stmt instanceof ForStatement) {
        	Statement bodystmt = ((ForStatement) stmt).getBody();
        	List<IndexComponent> bodystmt_ic_list = getIndexComponentsFromBodyStmt(cu, bodystmt, index_k);
        	for (IndexComponent bodystmt_ic : bodystmt_ic_list) {
        	    rslt_list.add(bodystmt_ic);
        	}
            }
            else if (stmt instanceof IfStatement) {
        	IfStatement ifstmt = (IfStatement) stmt;
        	List<IndexComponent> bodystmt_ic_list0 = getIndexComponentsFromBodyStmt(cu, ifstmt.getThenStatement(), index_k);
        	for (IndexComponent bodystmt_ic0 : bodystmt_ic_list0) {
        	    rslt_list.add(bodystmt_ic0);
        	}
        	List<IndexComponent> bodystmt_ic_list1 = getIndexComponentsFromBodyStmt(cu, ifstmt.getElseStatement(), index_k);
        	for (IndexComponent bodystmt_ic1 : bodystmt_ic_list1) {
        	    rslt_list.add(bodystmt_ic1);
        	}
            }
            else if (stmt instanceof SwitchStatement) {
        	SwitchStatement ss = (SwitchStatement) stmt;
        	List<IndexComponent> bodystmt_ic_list = getIndexComponentsForStmtObjList(cu, ss.statements(), index_k);
        	for (IndexComponent bodystmt_ic : bodystmt_ic_list) {
        	    rslt_list.add(bodystmt_ic);
        	}
            }
            else if (stmt instanceof SynchronizedStatement) {
        	Statement bodystmt = ((SynchronizedStatement) stmt).getBody();
        	List<IndexComponent> bodystmt_ic_list = getIndexComponentsFromBodyStmt(cu, bodystmt, index_k);
        	for (IndexComponent bodystmt_ic : bodystmt_ic_list) {
        	    rslt_list.add(bodystmt_ic);
        	}
            }
            else if (stmt instanceof TryStatement) {
        	TryStatement ts = (TryStatement) stmt;
        	List<IndexComponent> bodystmt_ic_list0 = getIndexComponentsFromBodyStmt(cu, ts.getBody(), index_k);
        	for (IndexComponent bodystmt_ic0 : bodystmt_ic_list0) {
        	    rslt_list.add(bodystmt_ic0);
        	}
        	@SuppressWarnings("unchecked") List<CatchClause> cc_list = ts.catchClauses();
        	for (CatchClause cc : cc_list) {
        	    List<IndexComponent> bodystmt_ic_list1 = getIndexComponentsFromBodyStmt(cu, cc.getBody(), index_k);
        	    for (IndexComponent bodystmt_ic1 : bodystmt_ic_list1) {
        		rslt_list.add(bodystmt_ic1);
        	    }
        	}
        	List<IndexComponent> bodystmt_ic_list2 = getIndexComponentsFromBodyStmt(cu, ts.getFinally(), index_k);
        	for (IndexComponent bodystmt_ic2 : bodystmt_ic_list2) {
        	    rslt_list.add(bodystmt_ic2);
        	}
            }
            else if (stmt instanceof WhileStatement) {
        	Statement bodystmt = ((WhileStatement) stmt).getBody();
        	List<IndexComponent> bodystmt_ic_list = getIndexComponentsFromBodyStmt(cu, bodystmt, index_k);
        	for (IndexComponent bodystmt_ic : bodystmt_ic_list) {
        	    rslt_list.add(bodystmt_ic);
        	}
            }
        }
        
        return rslt_list;
    }

    //bodystmt is a statement as the body of a compound statement
    //It could be either a block or a statement in other types.
    private List<IndexComponent> getIndexComponentsFromBodyStmt(CompilationUnit cu, Statement bodystmt, int index_k) {
        if (bodystmt == null) { return new ArrayList<IndexComponent>(); }
        if (bodystmt instanceof Block) {
            return getIndexComponentsForBlock(cu, ((Block) bodystmt), index_k);
        }
        else {
            List<Statement> bodystmt_list = new ArrayList<>();
            bodystmt_list.add(bodystmt);
            return getIndexComponentsForStmtObjList(cu, bodystmt_list, index_k); //this will add bodystmt as a single index component, and will add its children index components (if any exists).
        }
    }
}
