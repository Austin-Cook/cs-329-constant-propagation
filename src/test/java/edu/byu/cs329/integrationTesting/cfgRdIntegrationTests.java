package edu.byu.cs329.integrationTesting;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import edu.byu.cs329.TestUtils;
import edu.byu.cs329.cfg.ControlFlowGraph;
import edu.byu.cs329.cfg.ControlFlowGraphBuilder;
import edu.byu.cs329.cfg.StatementTracker;
import edu.byu.cs329.rd.ReachingDefinitions;
import edu.byu.cs329.rd.ReachingDefinitions.Definition;
import edu.byu.cs329.rd.ReachingDefinitionsBuilder;

public class cfgRdIntegrationTests {
  ControlFlowGraphBuilder cfgBuilderUnderTest = null;
  ReachingDefinitionsBuilder rdBuilderUnderTest = null;
  List<ControlFlowGraph> cfgList = null;
  ControlFlowGraph cfg = null;
  StatementTracker statementTracker = null;

  /**
   * METHOD 1
   * params(int a)
   * a = 1          -> end    // (a, *)
   * end                      // (a, "a = 1")
   * METHOD 2
   * params(int a)
   * a = 1          -> end    // (a, *)
   * end                      // (a, "a = 1")
   */
  @Test
  @Tag("Integration")
  @DisplayName("Should include CFG and RDs for both methods when given two methods")
  public void should_IncludeCfgAndRdForBoth_when_TwoMethods() {
    String fileName = "cfgRdIntegrationTestInputs/TwoMethods.java";
    init(fileName);
    assertEquals(2, cfgList.size());

    // METHOD 1

    cfg = cfgList.get(0);
    ReachingDefinitions reachingDefinitions = getReachingDefinitions(cfg);
    Statement aEq1 = statementTracker.getExpressionStatement(0);
    Statement end = cfg.getEnd();

    // cfg edges
    assertTrue(hasEdge(aEq1, end));

    // aEq1
    Set<Definition> aEq1Definitions = reachingDefinitions.getReachingDefinitions(aEq1);
    assertEquals(1, aEq1Definitions.size());
    assertTrue(doesDefine("a", null, aEq1Definitions));
    // end
    Set<Definition> endDefinitions = reachingDefinitions.getReachingDefinitions(end);
    assertEquals(1, endDefinitions.size());
    assertTrue(doesDefine("a", aEq1, endDefinitions));

    // METHOD 2

    cfg = cfgList.get(1);
    reachingDefinitions = getReachingDefinitions(cfg);
    aEq1 = statementTracker.getExpressionStatement(1);
    end = cfg.getEnd();

    // cfg edges
    assertTrue(hasEdge(aEq1, end));

    // aEq1
    aEq1Definitions = reachingDefinitions.getReachingDefinitions(aEq1);
    assertEquals(1, aEq1Definitions.size());
    assertTrue(doesDefine("a", null, aEq1Definitions));
    // end
    endDefinitions = reachingDefinitions.getReachingDefinitions(end);
    assertEquals(1, endDefinitions.size());
    assertTrue(doesDefine("a", aEq1, endDefinitions));
  }

  /**
   * params(int a)
   * while (true)   -> aEq1, bDecl  // (a, *), (a, "a = 2")
   *   a = 1        -> aEq2         // (a, *), (a, "a = 2")
   *   a = 2        -> whileStmt    // (a, 1)
   * boolean b      -> ifStmt       // (a, *), (a, "a = 2")
   * if (true)      -> aEq3, aEq4   // (a, *), (a, "a = 2"), (b, "boolean b")
   *   a = 3        -> end          // (a, *), (a, "a = 2"), (b, "boolean b")
   * else
   *   a = 4        -> returnStmt   // (a, *), (a, "a = 2"), (b, "boolean b")
   *   return       -> end          // (a, "a = 4"), (b, "boolean b")
   * end                            // (a, "a = 3"), (a, "a = 4"), (b, "boolean b")
   */
  @Test
  @Tag("Integration")
  @DisplayName("Should include all definitions when given a variety of statements")
  public void should_IncludeAllDefinitions_when_VarietyOfStatements() {
    String fileName = "cfgRdIntegrationTestInputs/IntegratedStatements.java";
    init(fileName);
    assertEquals(1, cfgList.size());
    cfg = cfgList.get(0);

    ReachingDefinitions reachingDefinitions = getReachingDefinitions(cfg);
    Statement whileStatement = statementTracker.getWhileStatement(0);
    Statement aEq1 = statementTracker.getExpressionStatement(0);
    Statement aEq2 = statementTracker.getExpressionStatement(1);
    Statement bDecl = statementTracker.getVariableDeclarationStatement(0);
    Statement ifStatement = statementTracker.getIfStatement(0);
    Statement aEq3 = statementTracker.getExpressionStatement(2);
    Statement aEq4 = statementTracker.getExpressionStatement(3);
    Statement returnStatement = statementTracker.getReturnStatement(0);
    Statement end = cfg.getEnd();

    assertAll(
      () -> assertTrue(hasEdge(whileStatement, aEq1)),
      () -> assertTrue(hasEdge(whileStatement, bDecl)),
      () -> assertTrue(hasEdge(aEq1, aEq2)),
      () -> assertTrue(hasEdge(aEq2, whileStatement)),
      () -> assertTrue(hasEdge(bDecl, ifStatement)),
      () -> assertTrue(hasEdge(ifStatement, aEq3)),
      () -> assertTrue(hasEdge(ifStatement, aEq4)),
      () -> assertTrue(hasEdge(aEq3, end)),
      () -> assertTrue(hasEdge(aEq4, returnStatement)),
      () -> assertTrue(hasEdge(returnStatement, end)),
      () -> assertFalse(hasEdge(whileStatement, end)),
      () -> assertFalse(hasEdge(ifStatement, end))
    );

    // whileStatement
    Set<Definition> whileDefinitions = reachingDefinitions.getReachingDefinitions(whileStatement);
    assertEquals(2, whileDefinitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", null, whileDefinitions)),
      () -> assertTrue(doesDefine("a", aEq2, whileDefinitions))
    );

    // aEq1
    Set<Definition> aEq1Definitions = reachingDefinitions.getReachingDefinitions(aEq1);
    assertEquals(2, aEq1Definitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", null, aEq1Definitions)),
      () -> assertTrue(doesDefine("a", aEq2, aEq1Definitions))
    );

    // aEq2
    Set<Definition> aEq2Definitions = reachingDefinitions.getReachingDefinitions(aEq2);
    assertEquals(1, aEq2Definitions.size());
    assertTrue(doesDefine("a", aEq1, aEq2Definitions));

    // bDecl (a, *), (a, "a = 2")
    Set<Definition> bDeclDefinitions = reachingDefinitions.getReachingDefinitions(bDecl);
    assertEquals(2, bDeclDefinitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", null, bDeclDefinitions)),
      () -> assertTrue(doesDefine("a", aEq2, bDeclDefinitions))
    );

    // ifStatement
    Set<Definition> ifDefinitions = reachingDefinitions.getReachingDefinitions(ifStatement);
    assertEquals(3, ifDefinitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", null, ifDefinitions)),
      () -> assertTrue(doesDefine("a", aEq2, ifDefinitions)),
      () -> assertTrue(doesDefine("b", bDecl, ifDefinitions))
    );

    // aEq3
    Set<Definition> aEq3Definitions = reachingDefinitions.getReachingDefinitions(aEq3);
    assertEquals(3, aEq3Definitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", null, aEq3Definitions)),
      () -> assertTrue(doesDefine("a", aEq2, aEq3Definitions)),
      () -> assertTrue(doesDefine("b", bDecl, aEq3Definitions))
    );

    // aEq4
    Set<Definition> aEq4Definitions = reachingDefinitions.getReachingDefinitions(aEq4);
    assertEquals(3, aEq4Definitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", null, aEq4Definitions)),
      () -> assertTrue(doesDefine("a", aEq2, aEq4Definitions)),
      () -> assertTrue(doesDefine("b", bDecl, aEq4Definitions))
    );

    // returnStatement (a, "a = 4"), (b, "boolean b")
    Set<Definition> returnDefinitions = reachingDefinitions.getReachingDefinitions(returnStatement);
    assertEquals(2, returnDefinitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", aEq4, returnDefinitions)),
      () -> assertTrue(doesDefine("b", bDecl, returnDefinitions))
    );

    // end (a, "a = 3"), (a, "a = 4"), (b, "boolean b")
    Set<Definition> endDefinitions = reachingDefinitions.getReachingDefinitions(end);
    assertEquals(3, endDefinitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", aEq3, endDefinitions)),
      () -> assertTrue(doesDefine("a", aEq4, endDefinitions)),
      () -> assertTrue(doesDefine("b", bDecl, endDefinitions))
    );
  }

  private boolean doesDefine(String name, Statement statement, final Set<Definition> definitions) {
    for (Definition definition : definitions) {
      if (definition.name.getIdentifier().equals(name) && definition.statement == statement) {
        return true;
      }
    }
    return false;
  }

  void init(String fileName) {
    ASTNode node = TestUtils.getASTNodeFor(this, fileName);
    ControlFlowGraphBuilder cfgBuilder = new ControlFlowGraphBuilder();
    cfgList = cfgBuilder.build(node);
    statementTracker = new StatementTracker(node);
    rdBuilderUnderTest = new ReachingDefinitionsBuilder();
  }

  private ReachingDefinitions getReachingDefinitions(ControlFlowGraph controlFlowGraph) {
    List<ControlFlowGraph> list = new ArrayList<ControlFlowGraph>();
    list.add(controlFlowGraph);
    List<ReachingDefinitions> reachingDefinitionsList = rdBuilderUnderTest.build(list);
    assertEquals(1, reachingDefinitionsList.size());
    return reachingDefinitionsList.get(0);
  }

  private boolean hasEdge(Statement source, Statement dest) {
    Set<Statement> successors = cfg.getSuccs(source);
    Set<Statement> predecessors = cfg.getPreds(dest);
    return successors != null && successors.contains(dest) 
        && predecessors != null && predecessors.contains(source);
  }
}
