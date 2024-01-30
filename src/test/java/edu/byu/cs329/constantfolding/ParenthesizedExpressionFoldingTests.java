package edu.byu.cs329.constantfolding;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.byu.cs329.TestUtils;

@DisplayName("Tests for folding ParenthesizedExpression types")
public class ParenthesizedExpressionFoldingTests {
  ParenthesizedExpressionFolding folderUnderTest = null;

  @BeforeEach
  void beforeEach() {
    folderUnderTest = new ParenthesizedExpressionFolding();
  }

  @Test
  @DisplayName("Should throw RuntimeException when root is null")
  void should_ThrowRuntimeException_when_RootIsNull() {
    assertThrows(RuntimeException.class, () -> {
      folderUnderTest.fold(null);
    });
  }

  @Test
  @DisplayName("Should throw RuntimeException when root is not a CompilationUnit and has no parent")
  void should_ThrowRuntimeException_when_RootIsNotACompilationUnitAndHasNoParent() {
    assertThrows(RuntimeException.class, () -> {
      URI uri = TestUtils.getUri(this, "");
      ASTNode compilationUnit = TestUtils.getCompilationUnit(uri);
      ASTNode root = compilationUnit.getAST().newNullLiteral();
      folderUnderTest.fold(root);
    });
  }

  @Test
  @DisplayName("Should not fold anything when there are no parenthesized literals")
  void should_NotFoldAnything_when_ThereAreNoParenthesizedLiterals() {
    String rootName = "foldingInputs/parenthesizedLiterals/should_NotFoldAnything_when_ThereAreNoParenthesizedLiterals.java";
    String expectedName = "foldingInputs/parenthesizedLiterals/should_NotFoldAnything_when_ThereAreNoParenthesizedLiterals.java";
    TestUtils.assertDidNotFold(this, rootName, expectedName, folderUnderTest);
  }

  @Test
  @DisplayName("Should fold when given parethesized boolean literal")
  void should_fold_when_GivenBooleanLiteral() {
    String rootName = "foldingInputs/parenthesizedLiterals/should_fold_when_GivenBooleanLiteral-root.java";
    String expectedName = "foldingInputs/parenthesizedLiterals/should_fold_when_GivenBooleanLiteral.java";
    TestUtils.assertDidFold(this, rootName, expectedName, folderUnderTest);
  }

  @Test
  @DisplayName("Should fold when given parethesized character literal")
  void should_fold_when_GivenCharacterLiteral() {
    String rootName = "foldingInputs/parenthesizedLiterals/should_fold_when_GivenCharacterLiteral-root.java";
    String expectedName = "foldingInputs/parenthesizedLiterals/should_fold_when_GivenCharacterLiteral.java";
    TestUtils.assertDidFold(this, rootName, expectedName, folderUnderTest);
  }
  
  @Test
  @DisplayName("Should fold when given parethesized null literal")
  void should_fold_when_givenNullLiteral() {
    String rootName = "foldingInputs/parenthesizedLiterals/should_fold_when_givenNullLiteral-root.java";
    String expectedName = "foldingInputs/parenthesizedLiterals/should_fold_when_givenNullLiteral.java";
    TestUtils.assertDidFold(this, rootName, expectedName, folderUnderTest);
  }
  
  @Test
  @DisplayName("Should fold when given parethesized string literal")
  void should_fold_when_givenStringLiteral() {
    String rootName = "foldingInputs/parenthesizedLiterals/should_fold_when_givenStringLiteral-root.java";
    String expectedName = "foldingInputs/parenthesizedLiterals/should_fold_when_givenStringLiteral.java";
    TestUtils.assertDidFold(this, rootName, expectedName, folderUnderTest);
  }
  
  @Test
  @DisplayName("Should fold when given parethesized type literal")
  void should_fold_when_givenTypeLiteral() {
    String rootName = "foldingInputs/parenthesizedLiterals/should_fold_when_givenTypeLiteral-root.java";
    String expectedName = "foldingInputs/parenthesizedLiterals/should_fold_when_givenTypeLiteral.java";
    TestUtils.assertDidFold(this, rootName, expectedName, folderUnderTest);
  }

  @Test
  @DisplayName("Should fold when given parethesized number literal")
  void should_fold_when_givenNumberLiteral() {
    String rootName = "foldingInputs/parenthesizedLiterals/should_fold_when_givenNumberLiteral-root.java";
    String expectedName = "foldingInputs/parenthesizedLiterals/should_fold_when_givenNumberLiteral.java";
    TestUtils.assertDidFold(this, rootName, expectedName, folderUnderTest);
  }
}