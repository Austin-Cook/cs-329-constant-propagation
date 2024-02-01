package edu.byu.cs329.constantfolding;

import edu.byu.cs329.utils.ExceptionUtils;
import edu.byu.cs329.utils.TreeModificationUtils;
import java.util.List;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Replaces plus infix literals with the negation
 * of the literal.
 *
 * @author Austin Cook
 *         Modeled after ParenthesizedExpressionFolding by Eric Mercer
 */
public class PlusInfixExpressionFolding implements Folding {
  static final Logger log = LoggerFactory.getLogger(PlusInfixExpressionFolding.class);

  public PlusInfixExpressionFolding() {
  }

  class Visitor extends ASTVisitor {
    public boolean didFold = false;

    @Override
    public void endVisit(InfixExpression n) {
      // check if uses '+' operator
      InfixExpression.Operator operator = n.getOperator();
      if (operator != InfixExpression.Operator.PLUS) {
        return;
      }

      // check that left and right operands are number literals
      if (!(n.getLeftOperand() instanceof NumberLiteral)
          || !(n.getRightOperand() instanceof NumberLiteral)) {
        return;
      }

      // check that all extended operands are number literals
      @SuppressWarnings("unchecked")
      List<Expression> expressions = n.extendedOperands();
      for (int i = 0; i < expressions.size(); i++) {
        if (!(expressions.get(i) instanceof NumberLiteral)) {
          return;
        }
      }

      // get simplified value
      int value = Integer.parseInt(((NumberLiteral) n.getLeftOperand()).getToken())
                  + Integer.parseInt(((NumberLiteral) n.getRightOperand()).getToken());
      for (int i = 0; i < expressions.size(); i++) {
        value += Integer.parseInt(((NumberLiteral) expressions.get(i)).getToken());
      }

      // make the swap
      AST ast = n.getAST();
      NumberLiteral newNode = ast.newNumberLiteral(Integer.toString(value));
      TreeModificationUtils.replaceChildInParent(n, newNode);
      didFold = true;
    }
  }

  /**
   * Replaces the plus infix literals in the tree with the added value
   * of the literals.
   * 
   * <p>Visits the root and any reachable nodes from the root to replace
   * any InfixExpression reachable node containing the plus operator and
   * only NumberLiterals for the left and right operatands and extendedOperands
   * with the added value of the NumberLiterals themselves.
   * 
   * <p>top := all nodes reachable from root such the each node
   *           is a plus infix expression that ends in all
   *           NumberLiterals
   * 
   * <p>parents := all nodes such that each one is the parent
   *               of some node in top
   * 
   * <p>isPlusInfixExpression(n) := isInfixExpression(n) /\
   *                                (getOperator(n) == '+')
   * 
   * <p>isFoldable(n) := isPlusInfixExpression(n)
   *                     /\ ( isNumberLiteral(getLeftOperand(n)) /\
   *                          isNumberLiteral(getRightOperand(n)) /\
   *                          ( forall m | extendedOperands(n) :: 
   *                            isNumberLiteral(m) )
   *                        )
   * 
   * <p>numberLiteral(n) := sum of the left and right operands and all
   *                        extended operands
   *
   * @modifies nodes in parents
   *
   * @requires root != null
   * @requires (root instanceof CompilationUnit) \/ parent(root) != null
   *
   * @ensures fold(root) == (old(top) != emptyset)
   * @ensures forall n in old(top), exists n' in nodes
   *             fresh(n')
   *          /\ isNumberLiteral(n')
   *          /\ value(n') == value(numberLiteral(n))
   *          /\ parent(n') == parent(n)
   *          /\ children(parent(n')) = (children(parent(n)) setminus {n}) union {n'}
   * 
   * @param root the root of the tree to traverse.
   * @return true if the plus infix literals were replaced in the rooted tree
   */
  public boolean fold(final ASTNode root) {
    checkRequires(root);
    Visitor visitor = new Visitor();
    root.accept(visitor);
    return visitor.didFold;
  }

  private void checkRequires(final ASTNode root) {
    ExceptionUtils.requiresNonNull(root, "Null root passed to PlusInfixExpressionFolding.fold");

    if (!(root instanceof CompilationUnit) && root.getParent() == null) {
      ExceptionUtils.throwRuntimeException(
          "Non-CompilationUnit root with no parent passed to PlusInfixExpressionFolding.fold"
      );
    }
  }
}
