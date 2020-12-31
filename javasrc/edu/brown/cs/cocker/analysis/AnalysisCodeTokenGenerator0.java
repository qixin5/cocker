package edu.brown.cs.cocker.analysis;

import java.util.List;
import java.util.ArrayList;
import edu.brown.cs.ivy.jcomp.*;
import org.eclipse.jdt.core.dom.*;

public class AnalysisCodeTokenGenerator0 extends AnalysisCodeTokenGenerator
{
    public final static String BOOL_LID = "$lb$";
    public final static String CHAR_LID = "$lc$";
    public final static String NUMBER_LID = "$ln$";
    public final static String STRING_LID = "$ls$";
    
    public AnalysisCodeTokenGenerator0() { super(); }

    protected boolean isJavaStandard(JcompType jctype) {

	if (jctype == null) { return false; }
	String full_name = jctype.getName();
	if (full_name == null) { return false; }
	else { return full_name.startsWith("java."); }
    }

    protected boolean isJavaStandard(JcompSymbol jcsymbol) {

	if (jcsymbol == null) { return false; }
	String full_name = jcsymbol.getFullName();
	if (full_name == null) { return false; }
	else { return full_name.startsWith("java."); }
    }

    @Override protected List<CodeToken> getCTs(FieldAccess fa, int prop) {

	Expression e0 = fa.getExpression();
	SimpleName sn1 = fa.getName();

	List<CodeToken> ct_list0 = getPatternForFieldAccess(e0, sn1, prop);
	if (!ct_list0.isEmpty()) { return ct_list0; }
	else {
	    List<CodeToken> ct_list = new ArrayList<CodeToken>();
	    append(ct_list, getCTs(e0, getNewProp(e0, prop)));
	    ct_list.add(new CodeToken(".", prop));
	    append(ct_list, getCTs(sn1, getNewProp(sn1, prop)));
	    return ct_list;
	}
    }

    @Override protected List<CodeToken> getCTs(QualifiedName qn, int prop) {

	Name n0 = qn.getQualifier();
	SimpleName sn1 = qn.getName();

	List<CodeToken> ct_list0 = getPatternForFieldAccess(n0, sn1, prop);
	if (!ct_list0.isEmpty()) { return ct_list0; }
	else {
	    List<CodeToken> ct_list = new ArrayList<CodeToken>();
	    append(ct_list, getCTs(n0, getNewProp(n0, prop)));
	    ct_list.add(new CodeToken(".", prop));
	    append(ct_list, getCTs(sn1, getNewProp(sn1, prop)));
	    return ct_list;
	}
    }
    
    @Override protected List<CodeToken> getCTsForVSN(SimpleName sn, int prop) {

	List<CodeToken> ct_list = new ArrayList<CodeToken>();
	JcompSymbol jcsymbol = JcompAst.getReference(sn);
	if (isJavaStandard(jcsymbol)) {
	    ct_list.add(new CodeToken(sn.getIdentifier(), prop));
	}
	else {
	    ct_list.add(new CodeToken(VID, prop));
	}
	return ct_list;
    }

    @Override protected List<CodeToken> getCTsForTSN(SimpleName tsn, int prop) {

	List<CodeToken> ct_list= new ArrayList<CodeToken>();
	JcompType jctype = JcompAst.getJavaType(tsn);
	if (isJavaStandard(jctype)) {
	    ct_list.add(new CodeToken(tsn.getIdentifier(), prop));
	}
	else {
	    ct_list.add(new CodeToken(TID, prop));
	}
	return ct_list;
    }

    @Override protected List<CodeToken> getCTsForMSN(SimpleName msn, int prop) {

	List<CodeToken> ct_list= new ArrayList<CodeToken>();
	JcompSymbol jcsymbol = JcompAst.getReference(msn);
	if (isJavaStandard(jcsymbol)) {
	    ct_list.add(new CodeToken(msn.getIdentifier(), prop));
	}
	else {
	    ct_list.add(new CodeToken(MID, prop));
	}
	return ct_list;
    }

    @Override protected List<CodeToken> getCTs(PrimitiveType pt, int prop) {

	List<CodeToken> ct_list= new ArrayList<CodeToken>();
	ct_list.add(new CodeToken(pt.toString(), prop));
	return ct_list;
    }

    @Override protected List<CodeToken> getCTs(BooleanLiteral bl, int prop) {

	List<CodeToken> ct_list= new ArrayList<CodeToken>();
	if (bl != null) { 
	    ct_list.add(new CodeToken(BOOL_LID, prop));
	}
	return ct_list;
    }

    @Override protected List<CodeToken> getCTs(CharacterLiteral cl, int prop) {

	List<CodeToken> ct_list= new ArrayList<CodeToken>();
	if (cl != null) {
	    ct_list.add(new CodeToken(cl.getEscapedValue(), prop));
	}
	return ct_list;
    }

    @Override protected List<CodeToken> getCTs(NullLiteral nl, int prop) {

	List<CodeToken> ct_list= new ArrayList<CodeToken>();
	if (nl != null) {
	    ct_list.add(new CodeToken("null", prop));
	}
	return ct_list;
    }

    @Override protected List<CodeToken> getCTs(NumberLiteral nl, int prop) {

	List<CodeToken> ct_list= new ArrayList<CodeToken>();
	if (nl != null) {
	    ct_list.add(new CodeToken(NUMBER_LID, prop));
	}
	return ct_list;
    }

    @Override protected List<CodeToken> getCTs(StringLiteral sl, int prop) {

	List<CodeToken> ct_list= new ArrayList<CodeToken>();
	if (sl != null) {
	    String str_escaped = sl.getEscapedValue();
	    String token_str = null;
	    if (str_escaped.matches(".*\\s+.*")) { token_str = STRING_LID; }
	    else { token_str = str_escaped; }
	    ct_list.add(new CodeToken(token_str, prop));
	}
	return ct_list;
    }


    private List<CodeToken> getPatternForFieldAccess(Expression e0, SimpleName sn1, int prop) {
	List<CodeToken> ct_list = new ArrayList<CodeToken>();
	//If e0 is a Java type or is an object of Java type,
	//use the names of e0 and the referenced field.
	if (e0 instanceof SimpleName) {
	    SimpleName sn0 = (SimpleName) e0;
	    JcompType jctype = JcompAst.getJavaType(sn0);
	    if (jctype != null) {
		if (jctype.isArrayType() || isJavaStandard(jctype)) {
		    append(ct_list, getCTs(sn0, getNewProp(sn0, prop)));
		    ct_list.add(new CodeToken(".", prop));
		    ct_list.add(new CodeToken(sn1.getIdentifier(), prop));
		    return ct_list;
		}
	    }
	    else {
		JcompSymbol jcsymbol = JcompAst.getReference(sn0); //e.g., str
		if (jcsymbol != null) {
		    JcompType jcsymbol_type = jcsymbol.getType(); //e.g., String
		    if (jcsymbol_type != null) {
			if (jcsymbol_type.isArrayType() || isJavaStandard(jctype)) {
			    append(ct_list, getCTs(sn0, getNewProp(sn0, prop)));
			    ct_list.add(new CodeToken(".", prop));
			    ct_list.add(new CodeToken(sn1.getIdentifier(), prop));
			    return ct_list;
			}
		    }
		}
	    }
	}
	//Otherwise
	return ct_list;
    }
}
