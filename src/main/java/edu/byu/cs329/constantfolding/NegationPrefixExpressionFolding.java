package edu.byu.cs329.constantfolding;

import edu.byu.cs329.utils.ExceptionUtils;
import edu.byu.cs329.utils.TreeModificationUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Replaces negation prefix literals with the negation
 * of the literal.
 *
 * @author Austin Cook
 *         Modeled after ParenthesizedExpressionFolding by Eric Mercer
 */
public class NegationPrefixExpressionFolding implements Folding {
  static final Logger log = LoggerFactory.getLogger(NegationPrefixExpressionFolding.class);

  public NegationPrefixExpressionFolding() {
  }

  class Visitor extends ASTVisitor {
    public boolean didFold = false;

    @Override
    public void endVisit(PrefixExpression n) {
      // check if uses '!' operator
      PrefixExpression.Operator operator = n.getOperator();
      if (operator != PrefixExpression.Operator.NOT) {
        return;
      }

      // check if operand is boolean literal
      if (!(n.getOperand() instanceof BooleanLiteral)) {
        return;
      }

      // get simplified value
      boolean value = !(((BooleanLiteral) n.getOperand()).booleanValue());

      // make the swap
      AST ast = n.getAST();
      BooleanLiteral newNode = ast.newBooleanLiteral(value);
      TreeModificationUtils.replaceChildInParent(n, newNode);
      didFold = true;
    }
  }

  /**
   * Replaces negation prefix literals in the tree with the negated BooleanLiteral.
   *
   * <p>Visits the root and any reachable nodes from the root to replace
   * any PrefixExpression node where the operator is '!'  and the Expression is a 
   * BooleanLiteral with the negation of the literal itself.
   *
   * <p>Ex of PrefixExpression within PrefixExpression: !(!true)
   *
   * <p>top := all nodes reachable from root such that each node
   *           is an outermost PrefixExpression that ends in a literal
   *
   * <p>parents := all nodes such that each one is the parent of
   *               some node in top
   *
   * <p>isNegationPrefixExpression := isPrefixExpression /\ 
   *                                  (getOperator(n) == '!'')
   *
   * <p>isFoldable(n) := isNegationPrefixExpression(n) /\ 
   *                     ( getOperand(n) == BooleanLiteral
   *                       || isFoldable(getOperand(n)) )
   *
   * <p>booleanLiteral(n) := if isBooleanLiteral(n) then !n else booleanLiteral(getOperand(n))
   *
   * @modifies nodes in parents
   *
   * @requires root != null
   * @requires (root instanceof CompilationUnit) \/ parent(root) != null
   *
   * @ensures fold(root) == (old(top) != emptyset)
   * @ensures forall n in old(top), exists n' in nodes
   *             fresh(n')
   *          /\ isBooleanLiteral(n')
   *          /\ value(n') == value(booleanLiteral(n))
   *          /\ parent(n') == parent(n)
   *          /\ children(parent(n')) == (children(parent(n)) setminus{n}) union {n'}
   * @param root the root of the tree to traverse.
   * @return true if negation prefix literals were replaced in the rooted tree
   */
  public boolean fold(final ASTNode root) {
    checkRequires(root);
    Visitor visitor = new Visitor();
    root.accept(visitor);
    return visitor.didFold;
  }

  private void checkRequires(final ASTNode root) {
    ExceptionUtils.requiresNonNull(root, "Null root passed to "
        + "NegationPrefixExpressionFolding.fold");

    if (!(root instanceof CompilationUnit) && root.getParent() == null) {
      ExceptionUtils.throwRuntimeException(
          "Non-CompilationUnit root with no parent passed to NegationPrefixExpressionFolding.fold"
      );
    }
  }
}
