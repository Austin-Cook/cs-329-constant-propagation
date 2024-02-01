package edu.byu.cs329.constantfolding;

import edu.byu.cs329.utils.ExceptionUtils;
import edu.byu.cs329.utils.TreeModificationUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Replaces less than infix literals with the negation
 * of the literal.
 *
 * @author Austin Cook
 *         Modeled after ParenthesizedExpressionFolding by Eric Mercer
 */
public class LessThanInfixExpressionFolding implements Folding {
  static final Logger log = LoggerFactory.getLogger(LessThanInfixExpressionFolding.class);

  public LessThanInfixExpressionFolding() {
  }

  class Visitor extends ASTVisitor {
    public boolean didFold = false;

    @Override
    public void endVisit(InfixExpression n) {
      // check if uses '<' operator
      InfixExpression.Operator operator = n.getOperator();
      if (operator != InfixExpression.Operator.LESS) {
        return;
      }

      // check if left and right operands are number literals
      if (!(n.getLeftOperand() instanceof NumberLiteral)
            || !(n.getRightOperand() instanceof NumberLiteral)) {
        return;
      }

      // get expression value
      boolean value = (Integer.parseInt(((NumberLiteral) n.getLeftOperand()).getToken())
                      < Integer.parseInt(((NumberLiteral) n.getRightOperand()).getToken()));

      // make the swap
      AST ast = n.getAST();
      BooleanLiteral newNode = ast.newBooleanLiteral(value);
      TreeModificationUtils.replaceChildInParent(n, newNode);
      didFold = true;
    }
  }

  /**
 * Replaces the less than infix literals in the tree with the resolved
 * BooleanLiteral value.
 *
 * <p>Visits the root and any reachable nodes from the root to replace
 * and InfixExpression reachable node containing the less than operator
 * and only NumberLiterals for the left and right operands with the
 * BooleanLiteral value resulting from the expression.
 *
 * <p>top := all nodes reachable from root such that each node
 *           is a less than infix expression where the left and right
 *           operands are both NumberLiterals
 *
 * <p>parents := all nodes such that each one is the parent
 *               of some node in top
 *
 * <p>isLessThanInfixExpression(n) := isInfixExpression(n) /\
 *                                (getOperator(n) == '<')
 *
 * <p>isFoldable(n) := isLessThanInfixExpression(n)
 *                     /\ ( isNumberLiteral(getLeftOperand(n)) /\
 *                          isNumberLiteral(getRightOperand(n))
 *                        )
 *
 * <p>booleanLiteral(n) := resulting boolean value from the evaluating the
 *                         left and right operands with the '<' operator
 *
 * @modifies nodes in parents
 *
 * @requires root != null
 * @requires (root instanceof CompulationUnit) \/ parent(root) != null
 *
 * @ensures fold(root) == (old(top) != emptyset)
 * @ensures forall n in old(top), exists n' in nodes
 *            fresh(n')
 *         /\ isBooleanLiteral(n')
 *         /\ value(n') == value(booleanLitearl(n))
 *         /\ parent(n') == parent(n)
 *         /\ children(parent(n')) == (children(parent(n)) setminus {n}) union {n'}
 */
  public boolean fold(final ASTNode root) {
    checkRequires(root);
    Visitor visitor = new Visitor();
    root.accept(visitor);
    return visitor.didFold;
  }

  private void checkRequires(final ASTNode root) {
    ExceptionUtils.requiresNonNull(root, "Null root passed to LessThanInfixExpressionFolding.fold");

    if (!(root instanceof CompilationUnit) && root.getParent() == null) {
      ExceptionUtils.throwRuntimeException(
          "Non-CompilationUnit root with no parent passed to LessThanInfixExpressionFolding.fold"
      );
    }
  }
}
