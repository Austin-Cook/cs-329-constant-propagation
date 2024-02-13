package edu.byu.cs329.rd;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import edu.byu.cs329.cfg.ControlFlowGraph;

public class MockUtils {
  public static ControlFlowGraph newMockForEmptyMethodWithTwoParameters(String first, String second) {
    ControlFlowGraph cfg = mock(ControlFlowGraph.class);
    Statement statement = mock(Statement.class);
    when(cfg.getStart()).thenReturn(statement);
    MethodDeclaration methodDeclarion = mock(MethodDeclaration.class);
    VariableDeclaration firstParameter = newMockForVariableDeclaration(first);
    VariableDeclaration secondParameter = newMockForVariableDeclaration(second);
    List<VariableDeclaration> parameterList = new ArrayList<VariableDeclaration>();
    parameterList.add(firstParameter);
    parameterList.add(secondParameter);
    when(methodDeclarion.parameters()).thenReturn(parameterList);
    when(cfg.getMethodDeclaration()).thenReturn(methodDeclarion);
    return cfg;
  }

  public static VariableDeclaration newMockForVariableDeclaration(String name) {
    VariableDeclaration declaration = mock(VariableDeclaration.class);
    SimpleName simpleName = mock(SimpleName.class);
    when(simpleName.getIdentifier()).thenReturn(name);
    when(declaration.getName()).thenReturn(simpleName);
    return declaration;
  }

  public static ControlFlowGraph newMockForMethodWithVariableAssignmentAndOneParameter() {
    ControlFlowGraph cfg = mock(ControlFlowGraph.class);
    MethodDeclaration methodDeclarion = mock(MethodDeclaration.class);
    VariableDeclaration firstParameter = newMockForVariableDeclaration("a");
    List<VariableDeclaration> parameterList = new ArrayList<VariableDeclaration>();
    parameterList.add(firstParameter);
    ExpressionStatement expressionStatement = mock(ExpressionStatement.class);
    Assignment assignment = mock(Assignment.class);
    SimpleName simpleName = mock(SimpleName.class);
    Statement endStatement = mock(Statement.class);
    HashSet<Statement> aSuccs = new HashSet<>(Set.of(endStatement));
    HashSet<Statement> endPreds = new HashSet<>(Set.of(expressionStatement));
    when(cfg.getMethodDeclaration()).thenReturn(methodDeclarion);
    when(methodDeclarion.parameters()).thenReturn(parameterList);
    when(cfg.getStart()).thenReturn(expressionStatement);
    when(cfg.getEnd()).thenReturn(endStatement);
    when(expressionStatement.getExpression()).thenReturn(assignment);
    when(assignment.getLeftHandSide()).thenReturn(simpleName);
    when(simpleName.getIdentifier()).thenReturn("a");
    when(cfg.getSuccs(expressionStatement)).thenReturn(aSuccs);
    when(cfg.getPreds(endStatement)).thenReturn(endPreds);

    return cfg;
  }
}
