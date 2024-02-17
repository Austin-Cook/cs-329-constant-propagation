package edu.byu.cs329.cfg;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import edu.byu.cs329.TestUtils;

@DisplayName("Tests for ControlFlowBuilder")
public class ControlFlowBuilderTests {
  ControlFlowGraphBuilder unitUnderTest = null;
  ControlFlowGraph controlFlowGraph = null;
  StatementTracker statementTracker = null;

  @BeforeEach
  void beforeEach() {
    unitUnderTest = new ControlFlowGraphBuilder();
  }

  void init(String fileName) {
    ASTNode node = TestUtils.getASTNodeFor(this, fileName);
    List<ControlFlowGraph> cfgList = unitUnderTest.build(node);
    assertEquals(1, cfgList.size());
    controlFlowGraph = cfgList.get(0);
    statementTracker = new StatementTracker(node);
  }

  @Test
  @Tag("MethodDeclaration")
  @DisplayName("Should set start and end same when empty method declaration")
  void should_SetStartAndEndSame_when_EmptyMethodDeclaration() {
    String fileName = "cfgInputs/methodDeclarationInputs/should_SetStartAndEndSame_when_EmptyMethodDeclaration.java";
    init(fileName);
    assertAll("Method declaration with empty block",
        () -> assertNotNull(controlFlowGraph.getMethodDeclaration()),
        () -> assertEquals(controlFlowGraph.getStart(), controlFlowGraph.getEnd())
    );
  }

  @Test
  @Tag("MethodDeclaration")
  @DisplayName("Should set start to first statement and end different when non-empty method declaration")
  void should_SetStartToFirstStatementAndEndDifferent_when_NonEmptyMethodDeclaration() {
    String fileName = "cfgInputs/methodDeclarationInputs/should_SetStartToFirstStatementAndEndDifferent_when_NonEmptyMethodDeclaration.java";
    init(fileName);
    Statement start = controlFlowGraph.getStart();
    Statement end = controlFlowGraph.getEnd();
    Statement variableDeclStatement = statementTracker.getVariableDeclarationStatement(0);
    assertAll("Method declaration with non-empty block",
        () -> assertNotNull(controlFlowGraph.getMethodDeclaration()), 
        () -> assertNotEquals(start, end),
        () -> assertTrue(start == variableDeclStatement),
        () -> assertTrue(hasEdge(variableDeclStatement, end))
    );    
  }

  @Test
  @Tag("Block")
  @DisplayName("Should link all when block has no return")
  void should_LinkAll_when_BlockHasNoReturn() {
    String fileName = "cfgInputs/blockInputs/should_LinkAll_when_BlockHasNoReturn.java";
    init(fileName);
    Statement variableDeclaration = statementTracker.getVariableDeclarationStatement(0);
    Statement expressionStatement = statementTracker.getExpressionStatement(0);
    assertTrue(hasEdge(variableDeclaration, expressionStatement));
  }

  @Test
  @Tag("Block")
  @DisplayName("Should link to return when block has return") 
  void should_LinkToReturn_when_BlockHasReturn() {
    String fileName = "cfgInputs/blockInputs/should_LinkToReturn_when_BlockHasReturn.java";
    init(fileName);
    Statement variableDeclaration = statementTracker.getVariableDeclarationStatement(0);
    Statement expressionStatement = statementTracker.getExpressionStatement(0);
    Statement returnStatement = statementTracker.getReturnStatement(0);
    assertAll(
        () -> assertTrue(hasEdge(variableDeclaration, returnStatement)),
        () -> assertFalse(hasEdge(returnStatement, expressionStatement))
    );
  }

  /**
   * if:    -> ..1, end
   *  ..1
   *  ..2   -> end
   */
  @Test
  @Tag("IfStatement")
  @DisplayName("Should link all when no else block")
  void should_LinkAll_when_NoElseBlock() {
    String fileName = "cfgInputs/ifStatementInputs/should_LinkAll_when_NoElseBlock.java";
    init(fileName);
    Statement ifStatement = statementTracker.getIfStatement(0);
    Statement innerExpression1 = statementTracker.getExpressionStatement(0);
    Statement innerExpression2 = statementTracker.getExpressionStatement(1);
    Statement endExpression = statementTracker.getExpressionStatement(2);

    assertAll(
      () -> assertTrue(hasEdge(ifStatement, innerExpression1)),
      () -> assertTrue(hasEdge(innerExpression2, endExpression)),
      () -> assertTrue(hasEdge(ifStatement, endExpression))
    );
  }

  /**
   * if:    -> ..1, end
   *  ..1
   *  ret
   */
  @Test
  @Tag("IfStatement")
  @DisplayName("Should not link from return when no else block and then block returns")
  void should_NotLinkFromReturn_when_NoElseBlockAndThenBlockReturns() {
    String fileName = "cfgInputs/ifStatementInputs/should_NotLinkFromReturn_when_NoElseBlockAndThenBlockReturns.java";
    init(fileName);
    Statement ifStatement = statementTracker.getIfStatement(0);
    Statement innerExpression1 = statementTracker.getExpressionStatement(0);
    Statement returnStatement = statementTracker.getReturnStatement(0);
    Statement endExpression = statementTracker.getExpressionStatement(1);

    assertAll(
      () -> assertTrue(hasEdge(ifStatement, innerExpression1)),
      () -> assertFalse(hasEdge(returnStatement, endExpression)),
      () -> assertTrue(hasEdge(ifStatement, endExpression))
    );
  }

  /**
   * if:    -> ..1, ..3
   *  ..1
   *  ..2   -> end
   * else:
   *  ..3
   *  ..4   -> end
   */
  @Test
  @Tag("IfStatement")
  @DisplayName("Should link all when exists else block")
  void should_LinkAll_when_ExistsElseBlock() {
    String fileName = "cfgInputs/ifStatementInputs/should_LinkAll_when_ExistsElseBlock.java";
    init(fileName);
    Statement ifStatement = statementTracker.getIfStatement(0);
    Statement thenExpression1 = statementTracker.getExpressionStatement(0);
    Statement thenExpression2 = statementTracker.getExpressionStatement(1);
    Statement elseExpression1 = statementTracker.getExpressionStatement(2);
    Statement elseExpression2 = statementTracker.getExpressionStatement(3);
    Statement end = controlFlowGraph.getEnd();

    assertAll(
      () -> assertTrue(hasEdge(ifStatement, thenExpression1)),
      () -> assertTrue(hasEdge(thenExpression2, end)),
      () -> assertTrue(hasEdge(ifStatement, elseExpression1)),
      () -> assertTrue(hasEdge(elseExpression2, end)),
      () -> assertFalse(hasEdge(ifStatement, end))
    );
  }

  /**
   * if:    -> ..1, ..2
   *  ..1
   *  ret
   * else:
   *  ..2
   *  ret
   */
  @Test
  @Tag("IfStatement")
  @DisplayName("Should not link from return when exists else block and blocks return")
  void should_NotLinkFromReturn_when_ExistsElseBlockAndBlocksReturn() {
    String fileName = "cfgInputs/ifStatementInputs/should_NotLinkFromReturn_when_ExistsElseBlockAndBlocksReturn.java";
    init(fileName);
    Statement ifStatement = statementTracker.getIfStatement(0);
    Statement thenExpression1 = statementTracker.getExpressionStatement(0);
    Statement thenReturnStatement = statementTracker.getReturnStatement(0);
    Statement elseExpression1 = statementTracker.getExpressionStatement(1);
    Statement elseReturnStatement = statementTracker.getReturnStatement(1);
    Statement endExpression = statementTracker.getExpressionStatement(2);

    assertAll(
      () -> assertTrue(hasEdge(ifStatement, thenExpression1)),
      () -> assertTrue(hasEdge(ifStatement, elseExpression1)),
      () -> assertFalse(hasEdge(thenReturnStatement, endExpression)),
      () -> assertFalse(hasEdge(elseReturnStatement, endExpression)),
      () -> assertFalse(hasEdge(ifStatement, endExpression))
    );
  }

  /**
   * if:      -> end
   *  empty
   * else:
   *  empty
   */
  @Test
  @Tag("IfStatement")
  @DisplayName("Should link only to end when exists else block and all blocks are empty")
  void should_LinkOnlyToEnd_when_ExistsElseBlockAndAllBlocksAreEmpty() {
    String fileName = "cfgInputs/ifStatementInputs/should_LinkOnlyToEnd_when_ExistsElseBlockAndAllBlocksAreEmpty.java";
    init(fileName);
    Statement ifStatement = statementTracker.getIfStatement(0);
    Statement endExpression = statementTracker.getExpressionStatement(0);

    assertAll(
      () -> assertThrows(IndexOutOfBoundsException.class, () -> {
        statementTracker.getExpressionStatement(1);
      }),
      () -> assertTrue(hasEdge(ifStatement, endExpression))
    );
  }

  /**
   * while:   -> ..1, end
   *  ..1
   *  ..2     -> while
   */
  @Test
  @Tag("WhileStatement")
  @DisplayName("Should link all when exists while block")
  void should_LinkAll_when_ExistsWhileBlock() {
    String fileName = "cfgInputs/whileStatementInputs/should_LinkAll_when_ExistsWhileBlock.java";
    init(fileName);
    Statement whileStatement = statementTracker.getWhileStatement(0);
    Statement expression1 = statementTracker.getExpressionStatement(0);
    Statement expression2 = statementTracker.getExpressionStatement(1);
    Statement end = controlFlowGraph.getEnd();

    assertAll(
      () -> assertTrue(hasEdge(whileStatement, expression1)),
      () -> assertTrue(hasEdge(expression2, whileStatement)),
      () -> assertTrue(hasEdge(whileStatement, end)),
      () -> assertFalse(hasEdge(expression2, end))
    );
  }

  /**
   * while:   -> ..1, end
   *  ..1
   *  ret     -> end
   */
  @Test
  @Tag("WhileStatement")
  @DisplayName("Should not link from return when while block returns")
  void should_NotLinkFromReturn_when_WhileBlockReturns() {
    String fileName = "cfgInputs/whileStatementInputs/should_NotLinkFromReturn_when_WhileBlockReturns.java";
    init(fileName);
    Statement whileStatement = statementTracker.getWhileStatement(0);
    Statement expression1 = statementTracker.getExpressionStatement(0);
    Statement returnStatement = statementTracker.getReturnStatement(0);
    Statement end = controlFlowGraph.getEnd();

    assertAll(
      () -> assertTrue(hasEdge(whileStatement, expression1)),
      () -> assertTrue(hasEdge(whileStatement, end)),
      () -> assertTrue(hasEdge(returnStatement, end)),
      () -> assertFalse(hasEdge(returnStatement, whileStatement))  
    );
  }

  /**
   * while:   -> end
   *  empty
   */
  @Test
  @Tag("WhileStatement")
  @DisplayName("Should link only to end when while block is empty")
  void should_LinkOnlyToEnd_when_WhileBlockIsEmpty() {
    String fileName = "cfgInputs/whileStatementInputs/should_LinkOnlyToEnd_when_WhileBlockIsEmpty.java";
    init(fileName);

    Statement whileStatement = statementTracker.getWhileStatement(0);
    Statement end = controlFlowGraph.getEnd();

    assertAll(
      () -> assertThrows(IndexOutOfBoundsException.class, () -> {
        statementTracker.getExpressionStatement(0);
      }),
      () -> assertTrue(hasEdge(whileStatement, end))
    );
  }

  private boolean hasEdge(Statement source, Statement dest) {
    Set<Statement> successors = controlFlowGraph.getSuccs(source);
    Set<Statement> predecessors = controlFlowGraph.getPreds(dest);
    return successors != null && successors.contains(dest) 
        && predecessors != null && predecessors.contains(source);
  }
}
