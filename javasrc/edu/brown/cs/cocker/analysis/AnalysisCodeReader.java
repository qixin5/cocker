package edu.brown.cs.cocker.analysis;

import java.io.Reader;
import java.io.FilterReader;
import org.eclipse.jdt.core.dom.ASTNode;
import java.util.List;
import java.util.ArrayList;

public class AnalysisCodeReader extends FilterReader
{
    Reader rdr;        //The wrapped reader
    String loc;        //Code Fragment Locating String
    int ex_start_pos;  //The extended start position (in terms of the source file text)
    int ex_end_pos;     //The extended end position (in terms of the source file text)
    List<ASTNode> node_list;

    public AnalysisCodeReader(Reader rdr) throws NullPointerException {

	super(rdr);
	this.rdr = rdr;
	loc = null;
	ex_start_pos = -1;
	ex_end_pos = -1;
	node_list = new ArrayList<ASTNode>();
    }

    public void setReader(Reader rdr) { this.rdr = rdr; } 
    
    public Reader getReader() { return rdr; }
    
    public void setLocString(String loc) { this.loc = loc; }

    public String getLocString() { return loc; }

    public void setExtendedStartPosition(int ex_start_pos) {
	this.ex_start_pos = ex_start_pos;
    }

    public int getExtendedStartPosition() { return ex_start_pos; }

    public void setExtendedEndPosition(int ex_end_pos) { this.ex_end_pos = ex_end_pos; }

    public int getExtendedEndPosition() { return ex_end_pos; }

    public void addNode(ASTNode node) { node_list.add(node); }

    public void setNodeList(List<ASTNode> node_list) { this.node_list = node_list; }

    public List<ASTNode> getNodeList() { return node_list; }
}
