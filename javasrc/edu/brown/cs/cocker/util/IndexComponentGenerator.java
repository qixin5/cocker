package edu.brown.cs.cocker.util;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import java.util.List;


public interface IndexComponentGenerator
{
    public List<IndexComponent> getIndexComponentsForMD(MethodDeclaration md);

    public List<IndexComponent> getIndexComponentsForMD(MethodDeclaration md, int index_k);
}
