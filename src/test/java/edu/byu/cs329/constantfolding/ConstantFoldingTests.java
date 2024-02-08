package edu.byu.cs329.constantfolding;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.net.URI;
import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import edu.byu.cs329.TestUtils;

@DisplayName("Integration tests for ConstantFolding")
public class ConstantFoldingTests {
  
  /**
   * Tests requires 1: root != null
   */
  @Test
  @DisplayName("Should throw RuntimeException when root is null")
  @Tag("precondition")
  void should_ThrowRuntimeException_when_RootIsNull() {
    assertThrows(RuntimeException.class, () -> {
      ConstantFolding.fold(null);
    });
  }

  /**
   * Tests requires 2: (root instanceof CompilationUnit) \/ parent(root)
   */
  @Test
  @DisplayName("Should throw RuntimeException when root is not a CompilationUnit and has no parent")
  @Tag("precondition")
  void should_ThrowRuntimeException_when_RootIsNotACompilationUnitAndHasNoParent() {
    assertThrows(RuntimeException.class, () -> {
      URI uri = TestUtils.getUri(this, "");
      ASTNode compilationUnit = TestUtils.getCompilationUnit(uri);
      ASTNode root = compilationUnit.getAST().newNullLiteral();
      ConstantFolding.fold(root);
    });
  }

  /**
   * Tests ensures 1 (Should not fold)
   */
  @Test
  @DisplayName("Should not fold anything when there are no parenthesized literals")
  @Tag("postcondition")
  void should_NotFoldAnything_when_ThereAreNoParenthesizedLiterals() {
    String rootName = "foldingInputs/constantFoldingIntegrationInputs/should_notFoldAnything_when_thereIsNothingToFold.java";
    String expectedName = "foldingInputs/constantFoldingIntegrationInputs/should_notFoldAnything_when_thereIsNothingToFold.java";
    TestUtils.assertEquals_ConstantFolding(this, rootName, expectedName);
  }

  /**
   * Tests ensures 1
   */
  @Test
  @DisplayName("Should fold when given individual expression literals")
  @Tag("postcondition")
  void should_fold_when_GivenIndividualExpressionLiterals() {
    String rootName = "foldingInputs/constantFoldingIntegrationInputs/should_fold_when_givenIndividualExpressionLiterals-root.java";
    String expectedName = "foldingInputs/constantFoldingIntegrationInputs/should_fold_when_givenIndividualExpressionLiterals.java";
    TestUtils.assertEquals_ConstantFolding(this, rootName, expectedName);
  }

    /**
   * Tests ensures 1
   */
  @Test
  @DisplayName("Should fold when given mixed expression literals")
  @Tag("postcondition")
  void should_fold_when_GivenMixedExpressionLiterals() {
    String rootName = "foldingInputs/constantFoldingIntegrationInputs/should_fold_when_givenMixedExpressionLiterals-root.java";
    String expectedName = "foldingInputs/constantFoldingIntegrationInputs/should_fold_when_givenMixedExpressionLiterals.java";
    TestUtils.assertEquals_ConstantFolding(this, rootName, expectedName);
  }
}
