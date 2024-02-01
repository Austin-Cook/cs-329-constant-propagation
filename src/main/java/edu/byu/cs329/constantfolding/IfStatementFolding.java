package edu.byu.cs329.constantfolding;

import edu.byu.cs329.utils.ExceptionUtils;
import edu.byu.cs329.utils.TreeModificationUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Replaces if statement literals with its respective then
 * or else block given the value of the BooleanLiteral.
 *
 * @author Austin Cook
 *         Modeled after ParenthesizedExpressionFolding by Eric Mercer
 */
public class IfStatementFolding implements Folding {
  static final Logger log = LoggerFactory.getLogger(IfStatementFolding.class);

  public IfStatementFolding() {
  }

  class Visitor extends ASTVisitor {
    public boolean didFold = false;

    @Override
    public void endVisit(IfStatement n) {
      // check if expression is boolean literal
      ASTNode exp = n.getExpression();
      if (!(exp instanceof BooleanLiteral)) {
        return;
      }

      //get expression boolean value
      boolean value = ((BooleanLiteral) n.getExpression()).booleanValue();

      
      if (!value && (n.getElseStatement() == null)) {
        // no else block and expression is false
        // remove entire IfStatement
        TreeModificationUtils.removeChildInParent(n);
      } else {
        // keep only the respective block 
        // get the then or else block cooresponding to expression value
        ASTNode respBlock;
        if (value) {
          respBlock = n.getThenStatement();
        } else {
          respBlock = n.getElseStatement();
        }

        // swap the if statement for the block
        AST ast = n.getAST();
        ASTNode newExp = ASTNode.copySubtree(ast, respBlock);
        TreeModificationUtils.replaceChildInParent(n, newExp);
      }

      didFold = true;
    }
  }

  /**
   * Replaces if statements having BooleanLiterals as the expression in the tree
   * with one of the two blocks from the if statement.
   *
   * <p>Visits the root and any reachable nodes from the root to replace
   * any IfStatement reachable node containing a BooleanLiteral as the
   * expression with the 'then' block of the original IfStatement if the
   * BooleanLiteral is true, else the 'else' block.
   *
   * <p>top:= all nodes reachable from root such that each node
   *          is an if statement where the expression is a boolean
   *          literal
   *
   * <p>parents := all nodes such that each one is the parent
   *               of some node in top
   *
   * <p>isFoldable(n) := isIfStatement(n)
   *                     /\ isBooleanLiteral(getExpression(n))
   *
   * <p>block(n) := if getExpression(n) == true then getThenStatement(n)
   *                else getElseStatement(n)
   *
   * @modifies nodes in parents
   *
   * @requires root != null
   * @requires (root instanceof CompilationUnit) \/ parent(root) != null
   *
   * @ensues fold(root) = (old(top) != emptyset)
   * @ensures forall n in old(top), exists n' in nodes
   *            fresh(n')
   *         /\ isBlock(n')
   *         /\ n' == block(n)
   *         /\ parent(n') == parent(n)
   *         /\ children(parent(n')) = (children(parent(n)) setminus {n}) union {n'}
   *
   * @param root the root of the tree to traverse.
   * @return true if if statements were replaced in the rooted tree
   */
  public boolean fold(final ASTNode root) {
    checkRequires(root);
    Visitor visitor = new Visitor();
    root.accept(visitor);
    return visitor.didFold;
  }

  private void checkRequires(final ASTNode root) {
    ExceptionUtils.requiresNonNull(root, "Null root passed to IfStatementFolding.fold");

    if (!(root instanceof CompilationUnit) && root.getParent() == null) {
      ExceptionUtils.throwRuntimeException(
          "Non-CompilationUnit root with no parent passed to IfStatementFolding.fold"
      );
    }
  }
}
