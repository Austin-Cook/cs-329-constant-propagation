package edu.byu.cs329.constantfolding;

import org.eclipse.jdt.core.dom.ASTNode;

/** 
 * Interface for folding.
 * */
public interface Folding {
  public boolean fold(final ASTNode root);
}
