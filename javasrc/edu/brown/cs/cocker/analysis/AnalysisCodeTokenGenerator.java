package edu.brown.cs.cocker.analysis;

import java.util.List;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.*;


public class AnalysisCodeTokenGenerator extends CodeTokenGenerator
{
    public final static String LID = "$l$";
    public final static String VID = "$v$";
    public final static String GID = "$p$"; //General ID
    public final static String TID = "$t$";
    public final static String MID = "$m$";
    
    public AnalysisCodeTokenGenerator() { super(); }


    @Override protected List<CodeToken> getCTs(BooleanLiteral bl, int prop) {

	List<CodeToken> ct_list= new ArrayList<CodeToken>();
	ct_list.add(new CodeToken(LID, prop));
	return ct_list;
    }

    @Override protected List<CodeToken> getCTs(CharacterLiteral cl, int prop) {

	List<CodeToken> ct_list= new ArrayList<CodeToken>();
	ct_list.add(new CodeToken(LID, prop));
	return ct_list;
    }

    @Override protected List<CodeToken> getCTs(NullLiteral nl, int prop) {

	List<CodeToken> ct_list= new ArrayList<CodeToken>();
	ct_list.add(new CodeToken(LID, prop));
	return ct_list;
    }

    @Override protected List<CodeToken> getCTs(NumberLiteral nl, int prop) {

	List<CodeToken> ct_list= new ArrayList<CodeToken>();
	ct_list.add(new CodeToken(LID, prop));
	return ct_list;
    }

    @Override protected List<CodeToken> getCTs(StringLiteral sl, int prop) {

	List<CodeToken> ct_list= new ArrayList<CodeToken>();
	ct_list.add(new CodeToken(LID, prop));
	return ct_list;
    }

    @Override protected List<CodeToken> getCTsForVSN(SimpleName sn, int prop) {

	List<CodeToken> ct_list = new ArrayList<CodeToken>();
	ct_list.add(new CodeToken(VID, prop));
	return ct_list;
    }

    @Override protected List<CodeToken> getCTsForTSN(SimpleName tsn, int prop) {

	List<CodeToken> ct_list = new ArrayList<CodeToken>();
	ct_list.add(new CodeToken(TID, prop));
	return ct_list;
    }

    @Override protected List<CodeToken> getCTs(PrimitiveType pt, int prop) {

	List<CodeToken> ct_list = new ArrayList<CodeToken>();
	ct_list.add(new CodeToken(TID, prop));
	return ct_list;
    }

    @Override protected List<CodeToken> getCTs(WildcardType wct, int prop) {

	List<CodeToken> ct_list = new ArrayList<CodeToken>();
	ct_list.add(new CodeToken(TID, prop));
	return ct_list;
    }

    @Override protected List<CodeToken> getCTsForMSN(SimpleName msn, int prop) {

	List<CodeToken> ct_list = new ArrayList<CodeToken>();
	ct_list.add(new CodeToken(MID, prop));
	return ct_list;
    }
    

    /* Produce context code tokens for predicates. */
    public List<CodeToken> getQueryCTs(ASTNode node, int prop) {
       
       List<CodeToken> ct_list = new ArrayList<CodeToken>();
       if (node == null) { return ct_list; }
       
       if ((node instanceof Expression) ||
             (node instanceof VariableDeclaration)) {
          
          ASTNode parent = node.getParent();
          if (parent == null) { //Shouldn't happen.
             return append(ct_list, getCTs(node, prop));
           } 
          
          int parent_node_type = parent.getNodeType();
          if (parent_node_type == ASTNode.CATCH_CLAUSE) {
             if (node == ((CatchClause) parent).getException())  {
        	    ct_list.add(new CodeToken("catch", prop));
        	    ct_list.add(new CodeToken("(", prop));
        	    append(ct_list, getCTs(node, prop));
        	    ct_list.add(new CodeToken(")", prop));
        	    return ct_list;
              }
           }
          else if (parent_node_type == ASTNode.DO_STATEMENT) {
             if (node == ((DoStatement) parent).getExpression()) {
        	    ct_list.add(new CodeToken("while", prop));
        	    ct_list.add(new CodeToken("(", prop));
        	    append(ct_list, getCTs(node, prop));
        	    ct_list.add(new CodeToken(")", prop));
        	    return ct_list;
              }
           }
          else if (parent_node_type == ASTNode.IF_STATEMENT) {
             if (node == ((IfStatement) parent).getExpression()) {
        	    ct_list.add(new CodeToken("if", prop));
        	    ct_list.add(new CodeToken("(", prop));
        	    append(ct_list, getCTs(node, prop));
        	    ct_list.add(new CodeToken(")", prop));
        	    return ct_list;
              }
           }
          else if (parent_node_type == ASTNode.SWITCH_CASE) {
             if (((SwitchCase) parent).expressions().contains(node)) {
                ct_list.add(new CodeToken("case", prop));
                append(ct_list, getCTs(node, prop));
                ct_list.add(new CodeToken(":", prop));
                return ct_list;
              }
           }
          else if (parent_node_type == ASTNode.SWITCH_STATEMENT) {
             if (node == ((SwitchStatement) parent).getExpression()) {
                ct_list.add(new CodeToken("switch", prop));
                ct_list.add(new CodeToken("(", prop));
                append(ct_list, getCTs(node, prop));
                ct_list.add(new CodeToken(")", prop));
                return ct_list;
              }
           }
          else if (parent_node_type == ASTNode.SYNCHRONIZED_STATEMENT) {
             if (node == ((SynchronizedStatement) parent).getExpression()) {
                ct_list.add(new CodeToken("synchronized", prop));
                ct_list.add(new CodeToken("(", prop));
                append(ct_list, getCTs(node, prop));
                ct_list.add(new CodeToken(")", prop));
                return ct_list;
              }
           }
          else if (parent_node_type == ASTNode.WHILE_STATEMENT) {
             if (node == ((WhileStatement) parent).getExpression()) {
                ct_list.add(new CodeToken("while", prop));
                ct_list.add(new CodeToken("(", prop));
                append(ct_list, getCTs(node, prop));
                ct_list.add(new CodeToken(")", prop));
                return ct_list;
              }
           }	    
          
          //For all other cases
          return getCTs(node, prop);
        }
       
       else {
          return getCTs(node, prop);
        }
    }
}
