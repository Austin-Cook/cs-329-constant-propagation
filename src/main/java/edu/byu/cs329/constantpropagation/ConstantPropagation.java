package edu.byu.cs329.constantpropagation;

import edu.byu.cs329.cfg.ControlFlowGraph;
import edu.byu.cs329.cfg.ControlFlowGraphBuilder;
import edu.byu.cs329.constantfolding.ConstantFolding;
import edu.byu.cs329.rd.ReachingDefinitions;
import edu.byu.cs329.rd.ReachingDefinitionsBuilder;
import edu.byu.cs329.rd.ReachingDefinitions.Definition;
import edu.byu.cs329.utils.JavaSourceUtils;
import edu.byu.cs329.utils.TreeModificationUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.compiler.ast.Literal;
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

  /**
   * Performs constant propagation.
   *
   * @param node the root node for constant propagation.
   */
  public static void propagate(ASTNode node) {
    ControlFlowGraphBuilder cfgBuilder = new ControlFlowGraphBuilder();
    ReachingDefinitionsBuilder rdBuilder = new ReachingDefinitionsBuilder();
    boolean changeMade = true;
    while (changeMade) {
      changeMade = false;
      node = ConstantFolding.fold(node);
      List<ControlFlowGraph> cfgList = cfgBuilder.build(node);
      List<ReachingDefinitions> rdList = rdBuilder.build(cfgList);
      assert(cfgList.size() == rdList.size());
      for (int i = 0; i < cfgList.size(); i++) {
        if (removeRedundantVariablesInCfg(cfgList.get(i), rdList.get(i))) {
          changeMade = true;
        }
      }
    }
  }

  /**
   * Performs constant folding an a Java file.
   *
   * @param args args[0] is the file to fold and args[1] is where to write the
   *             output
   */
  public static void main(String[] args) {
    // if (args.length != 2) {
    //   log.error("Missing Java input file or output file on command line");
    //   System.out.println("usage: java DomViewer <java file to parse> <html file to write>");
    //   return;
    // }

    // File inputFile = new File(args[0]);
    // ASTNode node = JavaSourceUtils.getCompilationUnit(inputFile.toURI());
    // ConstantPropagation.propagate(node);

    // try {
    //   PrintWriter writer = new PrintWriter(args[1], "UTF-8");
    //   writer.print(node.toString());
    //   writer.close();
    // } catch (Exception e) {
    //   e.printStackTrace();
    // }


    // FIXME REVERT
    // File inputFile = new File("/home/austin/winter-2024-wsl/cs-329/labs/PropTest1-root.java");
    File inputFile = new File("/home/austin/winter-2024-wsl/cs-329/labs/constant-propagation-Austin-Cook/src/test/resources/constantPropagationInputs/should_NotPropagate_when_ThereAreTwoDefinitions-root.java");
    ASTNode node = JavaSourceUtils.getCompilationUnit(inputFile.toURI());
    ConstantPropagation.propagate(node);
    
    try {
      PrintWriter writer = new PrintWriter("/home/austin/winter-2024-wsl/cs-329/labs/Out.java", "UTF-8");
      // PrintWriter writer = new PrintWriter("/home/austin/winter-2024-wsl/cs-329/labs/PropTest1.java", "UTF-8");
      writer.print(node.toString());
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Replaces all variables in a control graph that are have only one reaching definition
   * and are assigned to a literal.
   *
   * @param cfg the ControlFlowGraph under propagation
   * @param rd the ReachingDefinitions for all statements in cfg
   * @return true if at least one variable was reduced to a literal, false otherwise
   */
  private static boolean removeRedundantVariablesInCfg(ControlFlowGraph cfg, ReachingDefinitions rd) {
    Set<Statement> visited = new HashSet<>();
    Statement start = cfg.getStart();
    return removeRedundantVariablesInStatement(start, cfg, rd, visited);
  }

  /**
   * Replaces all variables in a given statement that have only one reaching definition
   * and are assigned to a literal.
   *
   * @param n the statement in which to replace redundant variables
   * @param cfg the ControlFlowGraph under propagation
   * @param rd the ReachingDefinitions for all statements in cfg
   * @param visited all statements in cfg already visited
   * @return true if at least one variable was reduced to a literal, false otherwise
   */
  private static boolean removeRedundantVariablesInStatement(Statement n, ControlFlowGraph cfg,
      ReachingDefinitions rd, Set<Statement> visited) {
    Set<Definition> defs = rd.getReachingDefinitions(n);
    boolean changeMade = false;
    visited.add(n);

    // get number of reaching definitions for each variable
    System.out.println("NEW STATEMENT");
    System.out.print(n.toString());
    System.out.println("Total # defs: " + defs.size());
    Map<String, ArrayList<Statement>> varToDefStmtMap = new HashMap<>();
    for (Definition d : defs) {
      String varName = d.name.getIdentifier();
      if (!varToDefStmtMap.containsKey(varName)) {
        ArrayList<Statement> list = new ArrayList<>();
        list.add(d.statement);
        varToDefStmtMap.put(varName, list);
      } else {
        varToDefStmtMap.get(varName).add(d.statement);
      }
    }

    // propagate variables one at a time
    for (String varName : varToDefStmtMap.keySet()) {
      if (varToDefStmtMap.get(varName).size() != 1) {
        // more than one definition for the var reaches n
        System.out.println("Not exectly 1 RD, continuing");
        continue;
      }

      Statement defStmt = varToDefStmtMap.get(varName).get(0);
      if (defStmt == null) {
        // defined in parameter (from a static perspective, parameter values are unknown)
        continue;
      }

      // get occurances of var in n
      VarOccVisitor visitor = new VarOccVisitor();
      visitor.varToFind = varName;
      n.accept(visitor);
      ArrayList<SimpleName> varOccToReplace = visitor.varOccList;

      if (varOccToReplace.size() == 0) {
        // no occurrances of the var to replace
        continue;
      }

      System.out.println("Var with only 1 def: " + varName);
      System.out.println("Exists " + varOccToReplace.size() + " times in n");

      ASTNode defLiteral = getDefinitionLiteral(defStmt);
      if(defLiteral == null) {
        // definition is not a literal
        continue;
      }

      ASTNode defLiteralCopy = copyLiteral(defLiteral);
      for (SimpleName varOcc : varOccToReplace) {
        System.out.println("Relpacing a var");
        TreeModificationUtils.replaceChildInParent(varOcc, defLiteralCopy);
        changeMade = true;
      }
    }

    // check other statements
    Set<Statement> succs = cfg.getSuccs(n);
    if (!(succs == null)) {
      for (Statement succ : succs) {
        if (!visited.contains(succ) && succ != cfg.getEnd()) {
          removeRedundantVariablesInStatement(succ, cfg, rd, visited);
        }
      }
    }

    return changeMade;
  }

  /**
   * Gets the assigned value of a definition statement if value is literal.
   *
   * <p>A definition only occurs in either an ExpressionStatement or a VariableDeclarationStatement.
   *
   * @param n the statement in which a variable is difined
   * @return the literal value assigned by the statement, else null if not literal
   */
  private static ASTNode getDefinitionLiteral(Statement n) {
    ASTNode definitionLiteral = null;

    // get definition value if literal
    if (n instanceof ExpressionStatement) {
      // is ExpressionStatement
      ExpressionStatement exp = (ExpressionStatement) n;
      if (exp.getExpression() instanceof Assignment) {
        // is Assignment (case 1)
        Assignment assignment = (Assignment) exp.getExpression();
        Expression definition = assignment.getRightHandSide();
        if (isLiteralExpression(definition)) {
          // definition value is a literal
          definitionLiteral = definition;
        }
      }
    } else if (n instanceof VariableDeclarationStatement) {
      // is VariableDeclarationStatement (case 2)
      List<VariableDeclarationFragment> varDeclFragList =
          getVariableDeclarationFragmentList(((VariableDeclarationStatement) n).fragments());
      VariableDeclarationFragment varDeclFrag = varDeclFragList.get(0);
      Expression definition = varDeclFrag.getInitializer();
        if ((definition != null) && isLiteralExpression(definition)) {
          // definition value is a literal
          definitionLiteral = definition;
        }
    }

    return definitionLiteral;
  }

  private static boolean isLiteralExpression(ASTNode exp) {
    return (exp instanceof BooleanLiteral) 
        || (exp instanceof CharacterLiteral)
        || (exp instanceof NullLiteral)
        || (exp instanceof StringLiteral)
        || (exp instanceof TypeLiteral)
        || (exp instanceof NumberLiteral);
  }

  /**
   * Creates and returns a copy of a literal.
   *
   * @param exp the ASTNode that is the literal
   * @return a copy of the literal, or null exp is not a literal
   */
  private static ASTNode copyLiteral(ASTNode exp) {
    AST ast = exp.getAST();
    ASTNode copy = null;

    if (exp instanceof BooleanLiteral) {
      copy = ast.newBooleanLiteral(((BooleanLiteral) exp).booleanValue());
    } else if (exp instanceof CharacterLiteral) {
      copy = ast.newCharacterLiteral();
      ((CharacterLiteral) copy).setCharValue(((CharacterLiteral) exp).charValue());
    } else if (exp instanceof NullLiteral) {
      copy = ast.newNullLiteral();
    } else if (exp instanceof StringLiteral) {
      copy = ast.newStringLiteral();
      ((StringLiteral) copy).setLiteralValue(((StringLiteral) exp).getLiteralValue());
    } else if (exp instanceof TypeLiteral) {
      copy = ast.newTypeLiteral();
      Type oldType = ((TypeLiteral) exp).getType();
      Type copiedType = (Type) ASTNode.copySubtree(ast, oldType);
      ((TypeLiteral) copy).setType(copiedType);
    } else if (exp instanceof NumberLiteral) {
      copy = ast.newNumberLiteral(((NumberLiteral) exp).getToken());
    }

    return copy;
  }

  private static List<VariableDeclarationFragment> getVariableDeclarationFragmentList(Object list) {
    @SuppressWarnings("unchecked")
    List<VariableDeclarationFragment> varDeclList = (List<VariableDeclarationFragment>) (list);
    return varDeclList;
  }

  /**
   * Gets a list of all SimpleName ASTNode occurances in a statement matching a variable name.
   *
   * <p>Begin visit on the statement node in which Usage: statement.accept(varOccVisitor). Note that
   * this does NOT filter when there is not only one definition for the var which is also a literal 
   * - that filtering occurs elsewhere.
   */
  static class VarOccVisitor extends ASTVisitor {
    public ArrayList<SimpleName> varOccList = new ArrayList<>();
    public String varToFind = null;

    @Override
    public void endVisit(SimpleName simpleName) {
      assert(varToFind != null);
      if (simpleName.getIdentifier().equals(varToFind)) {
        
        varOccList.add(simpleName);
      }
    }
  }
}
