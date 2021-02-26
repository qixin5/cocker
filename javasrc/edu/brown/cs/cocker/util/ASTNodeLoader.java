package edu.brown.cs.cocker.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;

import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.jcomp.*;


public class ASTNodeLoader
{
    private static JcompControl jcc = new JcompControl();
    
    public static ASTNode getASTNode(File f) {
       String fctnt = null;
       try { fctnt = FileUtils.readFileToString(f, (String)null); }
       catch (IOException e) {
          IvyLog.logE("UTIL","Problem getting AST node",e);
	}
       return getASTNode(fctnt);
     }
    
    public static ASTNode getASTNode(String fctnt) {
       if (fctnt == null) { return null; }
       return JcompAst.parseSourceFile(fctnt);
     }
    
    public static ASTNode getResolvedASTNode(File f) {
       String fpath = f.getAbsolutePath();
       String fctnt = null;
       try { fctnt = FileUtils.readFileToString(f, (String)null); }
       catch (IOException e) {
          IvyLog.logE("UTIL","Problem getting AST Node:,e");
        }
       return getResolvedASTNode(fpath, fctnt);
     }
    
    public static ASTNode getResolvedASTNode(String fpath, String fctnt) {
	if (fpath == null || fctnt == null) { return null; }
	ASTSource ast_src = new ASTSource(fpath, fctnt);
	List<JcompSource> jcs_list = new ArrayList<JcompSource>();
	jcs_list.add(ast_src);
	JcompProject jcp = jcc.getProject(jcs_list);
	jcp.resolve();
	JcompSemantics semdata = jcc.getSemanticData(ast_src);
	ASTNode node = semdata.getAstNode();
	jcc.freeProject(jcp);
	return node;
    }
    
    private static class ASTSource implements JcompSource
    {
        String file_path;
        String file_text;
    
        public ASTSource(String fpath, String ftext) {
            this.file_path = fpath;
            this.file_text = ftext;
        }
    
        @Override public String getFileContents() { return file_text; }
    
        @Override public String getFileName() { return file_path; }
    }
}
