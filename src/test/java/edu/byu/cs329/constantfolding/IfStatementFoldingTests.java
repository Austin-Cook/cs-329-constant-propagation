package edu.byu.cs329.constantfolding;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import edu.byu.cs329.TestUtils;

@DisplayName("Tests for folding IfStatemene literal types")
public class IfStatementFoldingTests {
  IfStatementFolding folderUnderTest = null;

  @BeforeEach
  void beforeEach() {
    folderUnderTest = new IfStatementFolding();
  }

  /**
   * Tests requires 1: root != null
   */
  @Test
  @DisplayName("Should throw RuntimeException when root is null")
  @Tag("precondition")
  void should_throwRuntimeException_when_rootIsNull() {
    assertThrows(RuntimeException.class,() -> {
      folderUnderTest.fold(null);
    });
  }

  /**
   * Tests requires 2: (root instanceof CompilationUnit) \/ parent(root)
   */
  @Test
  @DisplayName("Should throw RuntimeException when root is not a CompilationUnit and has no parent")
  @Tag("precondition")
  void should_throwRuntimeException_when_rootIsNotACompilationUnitAndHasNoParent() {
    assertThrows(RuntimeException.class, () -> {
      URI uri = TestUtils.getUri(this, "");
      ASTNode compilationUnit = TestUtils.getCompilationUnit(uri);
      ASTNode root = compilationUnit.getAST().newNullLiteral();
      folderUnderTest.fold(root);
    });
  }

  /**
   * Tests ensures 1: fold(root) == (old(top) != emptyset) (Should not fold)
   */
  @Test
  @DisplayName("Should not fold anything when there are no if statement literals")
  @Tag("postcondition")
  void should_notFoldAnything_when_thereAreNoIfStatementLiterals() {
    String rootname = "foldingInputs/ifLiterals/should_notFoldAnything_when_thereAreNoIfStatementLiterals.java";
    String expectedName = "foldingInputs/ifLiterals/should_notFoldAnything_when_thereAreNoIfStatementLiterals.java";
    TestUtils.assertDidNotFold(this, rootname, expectedName, folderUnderTest);
  }

  /**
   * Tests ensures 1: fold(root) == (old(top) != emptyset) (Should fold)
   * Tests ensures 2: forall n in old(top), exists n' in nodes
   *            fresh(n')
   *         /\ isBlock(n')
   *         /\ n' == block(n)
   *         /\ parent(n') == parent(n)
   *         /\ children(parent(n')) = (children(parent(n)) setminus {n}) union {n'}
  */
  @Test
  @DisplayName("Should fold to then block when given true if statement literal")
  @Tag("postcondition")
  void should_folw_when_givenTrueIfStatementLiteral() {
    String rootname = "foldingInputs/ifLiterals/should_fold_when_givenTrueIfStatementLiteral-root.java";
    String expectedName = "foldingInputs/ifLiterals/should_fold_when_givenTrueIfStatementLiteral.java";
    TestUtils.assertDidFold(this, rootname, expectedName, folderUnderTest);
  }

  /**
    * Tests ensures 1: fold(root) == (old(top) != emptyset) (Should fold)
    * Tests ensures 2: forall n in old(top), exists n' in nodes
    *            fresh(n')
    *         /\ isBlock(n')
    *         /\ n' == block(n)
    *         /\ parent(n') == parent(n)
    *         /\ children(parent(n')) = (children(parent(n)) setminus {n}) union {n'}
    */
  @Test
  @DisplayName("Should fold to else block when given false if statement literal")
  @Tag("postcondition")
  void should_fold_when_givenFalseIfStatementLiteral() {
    String rootname = "foldingInputs/ifLiterals/should_fold_when_givenFalseIfStatementLiteral-root.java";
    String expectedName = "foldingInputs/ifLiterals/should_fold_when_givenFalseIfStatementLiteral.java";
    TestUtils.assertDidFold(this, rootname, expectedName, folderUnderTest);
  }

    /**
    * Tests ensures 1: fold(root) == (old(top) != emptyset) (Should fold)
    * Tests ensures 2: forall n in old(top), exists n' in nodes
    *            fresh(n')
    *         /\ isBlock(n')
    *         /\ n' == block(n)
    *         /\ parent(n') == parent(n)
    *         /\ children(parent(n')) = (children(parent(n)) setminus {n}) union {n'}
    */
    @Test
    @DisplayName("Should remove if statment when given false and no else block exists")
    @Tag("postcondition")
    void should_fold_when_givenFalseIfStatementLiteralAndNoElseBlockExists() {
      String rootname = "foldingInputs/ifLiterals/should_fold_when_givenFalseIfStatementLiteralAndNoElseBlock-root.java";
      String expectedName = "foldingInputs/ifLiterals/should_fold_when_givenFalseIfStatementLiteralAndNoElseBlock.java";
      TestUtils.assertDidFold(this, rootname, expectedName, folderUnderTest);
    }
}
