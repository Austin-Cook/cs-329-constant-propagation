package edu.byu.cs329.constantpropagation;

import edu.byu.cs329.cfg.ControlFlowGraph;
import edu.byu.cs329.cfg.ControlFlowGraphBuilder;
import edu.byu.cs329.constantfolding.ConstantFolding;
import edu.byu.cs329.rd.ReachingDefinitions;
import edu.byu.cs329.rd.ReachingDefinitions.Definition;
import edu.byu.cs329.rd.ReachingDefinitionsBuilder;
import edu.byu.cs329.utils.JavaSourceUtils;
import edu.byu.cs329.utils.TreeModificationUtils;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constant Propagation.
 *
 * @author Eric Mercer
 * @author Austin Cook
 */
public class ConstantPropagation {

  static final Logger log = LoggerFactory.getLogger(ConstantPropagation.class);

  private static boolean changeMade = true;
  private static ControlFlowGraph cfg = null;
  private static ReachingDefinitions rd = null;
  private static Set<Statement> visited = null;

  /**
   * Performs constant folding an a Java file.
   *
   * @param args args[0] is the file to fold and args[1] is where to write the
   *             output
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      log.error("Missing Java input file or output file on command line");
      System.out.println("usage: java DomViewer <java file to parse> <html file to write>");
      return;
    }

    File inputFile = new File(args[0]);
    ASTNode node = JavaSourceUtils.getCompilationUnit(inputFile.toURI());
    ConstantPropagation.propagate(node);

    try {
      PrintWriter writer = new PrintWriter(args[1], "UTF-8");
      writer.print(node.toString());
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Performs constant propagation.
   *
   * @param node the root node for constant propagation.
   */
  public static void propagate(ASTNode node) {
    ControlFlowGraphBuilder cfgBuilder = new ControlFlowGraphBuilder();
    ReachingDefinitionsBuilder rdBuilder = new ReachingDefinitionsBuilder();
    changeMade = true;
    while (changeMade) {
      changeMade = false;
      node = ConstantFolding.fold(node);
      List<ControlFlowGraph> cfgList = cfgBuilder.build(node);
      List<ReachingDefinitions> rdList = rdBuilder.build(cfgList);
      assert (cfgList.size() == rdList.size());
      for (int i = 0; i < cfgList.size(); i++) {
        initState(cfgList.get(i), rdList.get(i));
        traverseTree(cfg.getStart());
      }
    }
  }

  private static void traverseTree(Statement n) {
    visited.add(n);

    PropagationVisitor visitor = new PropagationVisitor(n);
    n.accept(visitor);

    // traverse to other statements
    Set<Statement> succs = cfg.getSuccs(n);
    if (!(succs == null)) {
      for (Statement succ : succs) {
        if (!visited.contains(succ) && succ != cfg.getEnd()) {
          traverseTree(succ);
        }
      }
    }
  }

  static class PropagationVisitor extends ASTVisitor {
    public ArrayList<SimpleName> varOccList = new ArrayList<>();
    private Set<Definition> defs;

    public PropagationVisitor(Statement n) {
      this.defs = rd.getReachingDefinitions(n);
    }

    @Override
    public void endVisit(IfStatement ifStatement) {
      replaceIfPossible(ifStatement.getExpression());
    }

    @Override
    public void endVisit(WhileStatement whileStatement) {
      replaceIfPossible(whileStatement.getExpression());
    }

    @Override
    public void endVisit(DoStatement doStatement) {
      replaceIfPossible(doStatement.getExpression());
    }

    @Override
    public void endVisit(InfixExpression infixExpression) {
      replaceIfPossible(infixExpression.getLeftOperand());
      replaceIfPossible(infixExpression.getRightOperand());
      List<Expression> extendedOperands = getExpressionList(infixExpression.extendedOperands());
      if (extendedOperands != null) {
        for (Expression extendedOperand : extendedOperands) {
          replaceIfPossible(extendedOperand);
        }
      }
    }

    @Override
    public void endVisit(PrefixExpression prefixExpression) {
      replaceIfPossible(prefixExpression.getOperand());
    }

    @Override
    public void endVisit(ParenthesizedExpression parenthesizedExpression) {
      replaceIfPossible(parenthesizedExpression.getExpression());
    }

    @Override
    public void endVisit(VariableDeclarationFragment variableDeclarationFragment) {
      replaceIfPossible(variableDeclarationFragment.getInitializer());
    }

    @Override
    public void endVisit(Assignment assignment) {
      replaceIfPossible(assignment.getRightHandSide());
    }

    @Override
    public void endVisit(MethodInvocation methodInvocation) {
      List<Expression> arguments = getExpressionList(methodInvocation.arguments());
      if (arguments != null) {
        for (Expression argument : arguments) {
          replaceIfPossible(argument);
        }
      }
    }

    @Override
    public void endVisit(ReturnStatement returnStatement) {
      replaceIfPossible(returnStatement.getExpression());
    }

    private void replaceIfPossible(Expression exp) {
      if (!(exp instanceof SimpleName)) {
        return;
      }
      SimpleName replacee = (SimpleName) exp;

      // 1) replacee has 1 rd defining a replacement
      Statement defStmt = getRdIfOnlyOne(replacee);
      if (defStmt == null) {
        return;
      }
      // 2) replacement defines a literal
      ASTNode replacement = extractDefinition(defStmt);
      if (replacement == null || !isLiteralExpression(replacement)) {
        return;
      }

      // swap for replacement
      ASTNode replacementCopy = copyLiteral(replacement);
      TreeModificationUtils.replaceChildInParent(replacee, replacementCopy);
      changeMade = true;
    }

    private Statement getRdIfOnlyOne(SimpleName replacee) {
      String replaceName = replacee.getIdentifier();
      Statement replacement = null;

      for (Definition d : defs) {
        if (replaceName.equals(d.name.getIdentifier())) {
          if (replacement != null) {
            // match already found - too many reaching definitions
            return null;
          }
          replacement = d.statement;
        }
      }

      return replacement;
    }

    private static ASTNode extractDefinition(Statement n) {
      ASTNode def = null;

      // get definition value if literal
      if (n instanceof ExpressionStatement) {
        // is ExpressionStatement
        ExpressionStatement exp = (ExpressionStatement) n;
        if (exp.getExpression() instanceof Assignment) {
          // is Assignment (case 1)
          Assignment assignment = (Assignment) exp.getExpression();
          def = assignment.getRightHandSide();
        }
      } else if (n instanceof VariableDeclarationStatement) {
        // is VariableDeclarationStatement (case 2)
        List<VariableDeclarationFragment> varDeclFragList =
            getVariableDeclarationFragmentList(((VariableDeclarationStatement) n).fragments());
        VariableDeclarationFragment varDeclFrag = varDeclFragList.get(0);
        if (varDeclFrag.getInitializer() != null) {
          def = varDeclFrag.getInitializer();
        }
      }

      return def;
    }

    private static boolean isLiteralExpression(ASTNode exp) {
      return (exp instanceof BooleanLiteral) 
          || (exp instanceof NumberLiteral);
    }

    private static ASTNode copyLiteral(ASTNode exp) {
      AST ast = exp.getAST();
      ASTNode copy = null;

      if (exp instanceof BooleanLiteral) {
        copy = ast.newBooleanLiteral(((BooleanLiteral) exp).booleanValue());
      } else if (exp instanceof NumberLiteral) {
        copy = ast.newNumberLiteral(((NumberLiteral) exp).getToken());
      }

      return copy;
    }

    private static List<VariableDeclarationFragment> 
        getVariableDeclarationFragmentList(Object list) {
      @SuppressWarnings("unchecked")
      List<VariableDeclarationFragment> varDeclList = (List<VariableDeclarationFragment>) (list);
      return varDeclList;
    }
  
    private static List<Expression> getExpressionList(Object list) {
      @SuppressWarnings("unchecked")
      List<Expression> expressionList = (List<Expression>) (list);
      return expressionList;
    }
  }

  private static void initState(ControlFlowGraph newCfg, ReachingDefinitions newRd) {
    cfg = newCfg;
    rd = newRd;
    visited = new HashSet<>();
  }
}
