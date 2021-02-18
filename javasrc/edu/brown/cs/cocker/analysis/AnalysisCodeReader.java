package edu.brown.cs.cocker.analysis;

import java.io.Reader;
import java.io.FilterReader;
import org.eclipse.jdt.core.dom.ASTNode;
import java.util.List;
import java.util.ArrayList;

public class AnalysisCodeReader extends FilterReader
{
    Reader code_reader;        //The wrapped reader
    String code_loc;        //Code Fragment Locating String
    int ex_start_pos;  //The extended start position (in terms of the source file text)
    int ex_end_pos;     //The extended end position (in terms of the source file text)
    List<ASTNode> node_list;

    public AnalysisCodeReader(Reader rdr) throws NullPointerException {
    
        super(rdr);
        this.code_reader = rdr;
        code_loc = null;
        ex_start_pos = -1;
        ex_end_pos = -1;
        node_list = new ArrayList<ASTNode>();
    }

    public void setReader(Reader rdr) { this.code_reader = rdr; } 
    
    public Reader getReader() { return code_reader; }
    
    public void setLocString(String loc) { this.code_loc = loc; }

    public String getLocString() { return code_loc; }

    public void setExtendedStartPosition(int spos) {
        this.ex_start_pos = spos;
    }

    public int getExtendedStartPosition() { return ex_start_pos; }

    public void setExtendedEndPosition(int epos) { this.ex_end_pos = epos; }

    public int getExtendedEndPosition() { return ex_end_pos; }

    public void addNode(ASTNode node) { node_list.add(node); }

    public void setNodeList(List<ASTNode> nodelist) { this.node_list = nodelist; }

    public List<ASTNode> getNodeList() { return node_list; }
}
