package edu.byu.cs329.constantfolding;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import edu.byu.cs329.TestUtils;

@DisplayName("Tests for folding NegationPrefixExpression types")
public class NegationPrefixExpressionFoldingTests {
  NegationPrefixExpressionFolding folderUnderTest = null;

  @BeforeEach
  void beforeEach() {
    folderUnderTest = new NegationPrefixExpressionFolding();
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
  @DisplayName("Should not fold anything when there are no negation prefix literals")
  @Tag("postcondition")
  void should_notFoldAnything_when_thereAreNoNegationPrefixLiterals() {
    String rootname = "foldingInputs/negationPrefixLiterals/should_NotFoldAnything_when_ThereAreNoNegationPrefixLiterals.java";
    String expectedName = "foldingInputs/negationPrefixLiterals/should_NotFoldAnything_when_ThereAreNoNegationPrefixLiterals.java";
    TestUtils.assertDidNotFold(this, rootname, expectedName, folderUnderTest);
  }

  /**
   * Tests ensures 1: fold(root) == (old(top) != emptyset) (Should fold)
   * Tests ensures 2: forall n in old(top), exists n' in nodes
   *             fresh(n')
   *          /\ isBooleanLiteral(n')
   *          /\ value(n') == booleanLiteral(n)
   *          /\ parent(n') == parent(n)
   *          /\ children(parent(n')) == (children(parent(n)) setminus{n}) union {n'}
   */
  @Test
  @DisplayName("Should fold when given negation prefix literal")
  @Tag("postcondition")
  void should_folw_when_givenNegationPrefixLiteral() {
    String rootname = "foldingInputs/negationPrefixLiterals/should_fold_when_givenBooleanLiteral-root.java";
    String expectedName = "foldingInputs/negationPrefixLiterals/should_fold_when_givenBooleanLiteral.java";
    TestUtils.assertDidFold(this, rootname, expectedName, folderUnderTest);
  }
}
