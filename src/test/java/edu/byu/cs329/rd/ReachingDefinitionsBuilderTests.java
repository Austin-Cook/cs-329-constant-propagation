package edu.byu.cs329.rd;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import edu.byu.cs329.TestUtils;
import edu.byu.cs329.cfg.ControlFlowGraph;
import edu.byu.cs329.cfg.ControlFlowGraphBuilder;
import edu.byu.cs329.cfg.StatementTracker;
import edu.byu.cs329.rd.ReachingDefinitions.Definition;

@DisplayName("Tests for ReachingDefinitionsBuilder")
public class ReachingDefinitionsBuilderTests {
  ReachingDefinitionsBuilder unitUnderTest = null;
  ControlFlowGraph cfg = null;
  StatementTracker statementTracker = null;

  @BeforeEach
  void beforeEach() {
    unitUnderTest = new ReachingDefinitionsBuilder();
  }

  @Test
  @Tag("Parameters")
  @DisplayName("Should have a definition for each parameter at start when the method declaration has parameters.")
  void should_HaveDefinitionForEachParameterAtStart_when_MethodDeclarationHasParameters() {
    ControlFlowGraph cfg = MockUtils.newMockForEmptyMethodWithTwoParameters("a", "b");
    ReachingDefinitions reachingDefinitions = getReachingDefinitions(cfg);
    Statement start = cfg.getStart();
    Set<Definition> definitions = reachingDefinitions.getReachingDefinitions(start);
    assertEquals(2, definitions.size());
    assertAll("Parameters Defined at Start", 
        () -> assertTrue(doesDefine("a", null, definitions)),
        () -> assertTrue(doesDefine("b", null, definitions))
    );
  }

  /**
   * params(int a)
   * a = 1          // (a, *)
   * end            // (a, "a = 1")
   */
  @Test
  @Tag("Assign")
  @Tag("Linear")
  @Tag("Mock")
  @DisplayName("Should update the definition for a varialbe when assigned and the graph is linear")
  void should_UpdateDefinitionForVariable_when_AssignedAndGraphIsLinear() {
    cfg = MockUtils.newMockForMethodWithVariableAssignmentAndOneParameter();

    ReachingDefinitions reachingDefinitions = getReachingDefinitions(cfg);
    Statement aEq1 = cfg.getStart();
    Statement end = cfg.getEnd();

    verify(cfg, times(1)).getMethodDeclaration();
    verify(cfg, atLeastOnce()).getStart();
    verify(cfg, atLeastOnce()).getEnd();
    verify(cfg, atLeastOnce()).getPreds(cfg.getStart());
    verify(cfg, atLeastOnce()).getPreds(cfg.getEnd());
    verify(cfg, atLeastOnce()).getSuccs(cfg.getStart());
    verify(cfg, atLeastOnce()).getSuccs(cfg.getEnd());

    // a = 1
    Set<Definition> aEq1Definitions = reachingDefinitions.getReachingDefinitions(aEq1);
    assertEquals(1, aEq1Definitions.size());
    assertTrue(doesDefine("a", null, aEq1Definitions));

    // end
    Set<Definition> endDefinitions = reachingDefinitions.getReachingDefinitions(end);
    assertEquals(1, endDefinitions.size());
    assertTrue(doesDefine("a", aEq1, endDefinitions));
  }

  /**
   * params(int a)
   * if (true)      // (a, *)
   *   a = 1        // (a, *)
   * else
   *   a = 2        // (a, *)
   * end            // (a, "a = 1"), (a, "a = 2")
   */
  @Test
  @Tag("Assign")
  @Tag("Branch")
  @Tag("Spy")
  @DisplayName("Should include both definitions for a variable at the intersection when assigned in both branches")
  void should_IncludeBothDefinitionsForVariableAtIntersection_when_AssignedInBothBranches() {
    String fileName = "rdInputs/AssignBranch.java";
    cfgInit(fileName);
    ControlFlowGraph spyCFG = spy(cfg);

    ReachingDefinitions reachingDefinitions = getReachingDefinitions(spyCFG);
    Statement ifStatement = statementTracker.getIfStatement(0);
    Statement aEq1 = statementTracker.getExpressionStatement(0);
    Statement aEq2 = statementTracker.getExpressionStatement(1);
    Statement end = spyCFG.getEnd();

    verify(spyCFG, times(1)).getMethodDeclaration();
    verify(spyCFG, atLeastOnce()).getStart();
    verify(spyCFG, atLeastOnce()).getEnd();
    verify(spyCFG, atLeastOnce()).getPreds(spyCFG.getStart());
    verify(spyCFG, atLeastOnce()).getPreds(spyCFG.getEnd());
    verify(spyCFG, atLeastOnce()).getSuccs(spyCFG.getStart());
    verify(spyCFG, atLeastOnce()).getSuccs(spyCFG.getEnd());

    // if (true)
    Set<Definition> ifDefinitions = reachingDefinitions.getReachingDefinitions(ifStatement);
    assertEquals(1, ifDefinitions.size());
    assertTrue(doesDefine("a", null, ifDefinitions));

    // a = 1
    Set<Definition> aEq1Definitions = reachingDefinitions.getReachingDefinitions(aEq1);
    assertEquals(1, aEq1Definitions.size());
    assertTrue(doesDefine("a", null, aEq1Definitions));

    // a = 2
    Set<Definition> aEq2Definitions = reachingDefinitions.getReachingDefinitions(aEq2);
    assertEquals(1, aEq2Definitions.size());
    assertTrue(doesDefine("a", null, aEq2Definitions));

    // end
    Set<Definition> endDefinitions = reachingDefinitions.getReachingDefinitions(end);
    assertEquals(2, endDefinitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", aEq1, endDefinitions)),
      () -> assertTrue(doesDefine("a", aEq2, endDefinitions))
    );
  }

  /**
   * params(int a)
   * while (true)   // (a, *), (a, "a = 2")
   *   a = 1        // (a, *), (a, "a = 2")
   *   a = 2        // (a, "a = 1")
   * end            // (a, *), (a, "a = 2")
   */
  @Test
  @Tag("Assign")
  @Tag("Loop")
  @DisplayName("Should include both definitions for a variable at the intersection when assigned before and in a loop")
  void should_IncludeBothDefinitionsForVariableAtIntersection_when_AssignedBeforeAndInLoop() {
    String fileName = "rdInputs/AssignLoop.java";
    cfgInit(fileName);
    ReachingDefinitions reachingDefinitions = getReachingDefinitions(cfg);
    Statement whileTrue = statementTracker.getWhileStatement(0);
    Statement aEq1 = statementTracker.getExpressionStatement(0);
    Statement aEq2 = statementTracker.getExpressionStatement(1);
    Statement end = cfg.getEnd();

    // while (true)
    Set<Definition> whileDefinitions = reachingDefinitions.getReachingDefinitions(whileTrue);
    assertEquals(2, whileDefinitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", null, whileDefinitions)),
      () -> assertTrue(doesDefine("a", aEq2, whileDefinitions))
    );

    // a = 1
    Set<Definition> aEq1Definitions = reachingDefinitions.getReachingDefinitions(aEq1);
    assertEquals(2, aEq1Definitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", null, aEq1Definitions)),
      () -> assertTrue(doesDefine("a", aEq2, aEq1Definitions))
    );

    // a = 2
    Set<Definition> aEq2Definitions = reachingDefinitions.getReachingDefinitions(aEq2);
    assertEquals(1, aEq2Definitions.size());
    assertTrue(doesDefine("a", aEq1, aEq2Definitions));

    // end
    Set<Definition> endDefinitions = reachingDefinitions.getReachingDefinitions(end);
    assertEquals(2, endDefinitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", null, endDefinitions)),
      () -> assertTrue(doesDefine("a", aEq2, endDefinitions))
    );
  }

  /**
   * params(int a)
   * if (true)      // (a, *)
   *   a = 1        // (a, *)
   *   return       // (a, "a = 1")
   * else
   *   a = 2        // (a, *)
   * boolean b      // (a, "a = 2")
   * end            // (a, "a = 1"), (a, "a = 2"), (b, "boolean b")
   */
  @Test
  @Tag("Assign")
  @Tag("Branch")
  @DisplayName("Should not include a definition from a branch when the branch returns")
  void should_NotIncludeDefinitionFromBranch_when_BranchReturns() {
    String fileName = "rdInputs/AssignBranchReturn.java";
    cfgInit(fileName);
    ReachingDefinitions reachingDefinitions = getReachingDefinitions(cfg);
    Statement ifStatement = statementTracker.getIfStatement(0);
    Statement aEq1 = statementTracker.getExpressionStatement(0);
    Statement returnStatement = statementTracker.getReturnStatement(0);
    Statement aEq2 = statementTracker.getExpressionStatement(1);
    Statement bDecl = statementTracker.getVariableDeclarationStatement(0);
    Statement end = cfg.getEnd();

    // if (true)
    Set<Definition> ifDefinitions = reachingDefinitions.getReachingDefinitions(ifStatement);
    assertEquals(1, ifDefinitions.size());
    assertTrue(doesDefine("a", null, ifDefinitions));

    // a = 1
    Set<Definition> aEq1Definitions = reachingDefinitions.getReachingDefinitions(aEq1);
    assertEquals(1, aEq1Definitions.size());
    assertTrue(doesDefine("a", null, aEq1Definitions));

    // return
    Set<Definition> returnDefinitions = reachingDefinitions.getReachingDefinitions(returnStatement);
    assertEquals(1, returnDefinitions.size());
    assertTrue(doesDefine("a", aEq1, returnDefinitions));

    // a = 2
    Set<Definition> aEq2Definitions = reachingDefinitions.getReachingDefinitions(aEq2);
    assertEquals(1, aEq2Definitions.size());
    assertTrue(doesDefine("a", null, aEq2Definitions));

    // boolean b
    Set<Definition> bDeclDefinitions = reachingDefinitions.getReachingDefinitions(bDecl);
    assertEquals(1, bDeclDefinitions.size());
    assertTrue(doesDefine("a", aEq2, bDeclDefinitions));

    // end
    Set<Definition> endDefinitions = reachingDefinitions.getReachingDefinitions(end);
    assertEquals(3, endDefinitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", aEq1, endDefinitions)),
      () -> assertTrue(doesDefine("a", aEq2, endDefinitions)),
      () -> assertTrue(doesDefine("b", bDecl, endDefinitions))
    );
  }

  /**
   * params(int a)
   * while (true)   // (a, *)
   *   a = 1        // (a, *)
   *   return       // (a, "a = 1")
   * boolean b      // (a, *)
   * end            // (a, *), (a, "a = 1"), (b, "boolean b")
   */
  @Test
  @Tag("Assign")
  @Tag("Loop")
  @DisplayName("Should not include a definition from a loop when the loop returns")
  void should_NotIncludeDefinitionFromLoop_when_LoopReturns() {
    String fileName = "rdInputs/AssignLoopReturn.java";
    cfgInit(fileName);
    ReachingDefinitions reachingDefinitions = getReachingDefinitions(cfg);
    Statement whileStatement = statementTracker.getWhileStatement(0);
    Statement aEq1 = statementTracker.getExpressionStatement(0);
    Statement returnStatement = statementTracker.getReturnStatement(0);
    Statement bDecl = statementTracker.getVariableDeclarationStatement(0);
    Statement end = cfg.getEnd();

    // while (true)
    Set<Definition> whileDefinitions = reachingDefinitions.getReachingDefinitions(whileStatement);
    assertEquals(1, whileDefinitions.size());
    assertTrue(doesDefine("a", null, whileDefinitions));

    // a = 1
    Set<Definition> aEq1Definitions = reachingDefinitions.getReachingDefinitions(aEq1);
    assertEquals(1, aEq1Definitions.size());
    assertTrue(doesDefine("a", null, aEq1Definitions));

    // return
    Set<Definition> returnDefinitions = reachingDefinitions.getReachingDefinitions(returnStatement);
    assertEquals(1, returnDefinitions.size());
    assertTrue(doesDefine("a", aEq1, returnDefinitions));

    // boolean b
    Set<Definition> bDeclDefinitions = reachingDefinitions.getReachingDefinitions(bDecl);
    assertEquals(1, bDeclDefinitions.size());
    assertTrue(doesDefine("a", null, bDeclDefinitions));

    // end
    Set<Definition> endDefinitions = reachingDefinitions.getReachingDefinitions(end);
    assertEquals(3, endDefinitions.size());
    assertAll(
      () -> assertTrue(doesDefine("a", null, endDefinitions)),
      () -> assertTrue(doesDefine("a", aEq1, endDefinitions)),
      () -> assertTrue(doesDefine("b", bDecl, endDefinitions))
    );
  }

  /**
   * params(empty)
   * int a;
   * end            // (a, "int a")
   */
  @Test
  @Tag("Declare")
  @Tag("Linear")
  @DisplayName("Should include a definition for a variable when declered in its predecessor")
  void should_IncludeDefinitionForVariable_when_DeclaredInPredecessor() {
    String fileName = "rdInputs/DeclareLinear.java";
    cfgInit(fileName);
    ReachingDefinitions reachingDefinitions = getReachingDefinitions(cfg);
    Statement aDecl = statementTracker.getVariableDeclarationStatement(0);
    Statement end = cfg.getEnd();

    // int a
    Set<Definition> aDeclDefinitions = reachingDefinitions.getReachingDefinitions(aDecl);
    assertEquals(0, aDeclDefinitions.size());

    // end
    Set<Definition> endDefinitions = reachingDefinitions.getReachingDefinitions(end);
    assertEquals(1, endDefinitions.size());
    assertTrue(doesDefine("a", aDecl, endDefinitions));
  }

  private boolean doesDefine(String name, Statement statement, final Set<Definition> definitions) {
    for (Definition definition : definitions) {
      if (definition.name.getIdentifier().equals(name) && definition.statement == statement) {
        return true;
      }
    }
    return false;
  }

  void cfgInit(String fileName) {
    ASTNode node = TestUtils.getASTNodeFor(this, fileName);
    ControlFlowGraphBuilder cfgBuilder = new ControlFlowGraphBuilder();
    List<ControlFlowGraph> cfgList = cfgBuilder.build(node);
    assertEquals(1, cfgList.size());
    cfg = cfgList.get(0);
    statementTracker = new StatementTracker(node);
  }

  private ReachingDefinitions getReachingDefinitions(ControlFlowGraph controlFlowGraph) {
    List<ControlFlowGraph> list = new ArrayList<ControlFlowGraph>();
    list.add(controlFlowGraph);
    List<ReachingDefinitions> reachingDefinitionsList = unitUnderTest.build(list);
    assertEquals(1, reachingDefinitionsList.size());
    return reachingDefinitionsList.get(0);
  }
}
