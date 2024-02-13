package edu.byu.cs329.rd;

import edu.byu.cs329.cfg.ControlFlowGraph;
import edu.byu.cs329.rd.ReachingDefinitions.Definition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;


/**
 * Builder for reaching definitions on a control flow graph.
 */
public class ReachingDefinitionsBuilder {
  private List<ReachingDefinitions> rdList = null;
  private Map<Statement, Set<Definition>> entrySetMap = null;
  private Map<Statement, Set<Definition>> exitSetMap = null;
  private Map<Statement, Set<SimpleName>> killSetMap = null;
  private Map<Statement, Set<SimpleName>> genSetMap = null;
  private ControlFlowGraph cfg = null;
  private Set<Definition> parameterDefinitions = null;

  /**
   * Computes the reaching definitions for each control flow graph.
   *
   * @param cfgList the list of control flow graphs.
   * @return the coresponding reaching definitions for each graph.
   */
  public List<ReachingDefinitions> build(List<ControlFlowGraph> cfgList) {
    rdList = new ArrayList<ReachingDefinitions>();
    for (ControlFlowGraph cfg : cfgList) {
      this.cfg = cfg;
      ReachingDefinitions rd = computeReachingDefinitions();
      rdList.add(rd);
    }
    return rdList;
  }

  private ReachingDefinitions computeReachingDefinitions() {
    entrySetMap = new HashMap<Statement, Set<Definition>>();
    exitSetMap = new HashMap<Statement, Set<Definition>>();
    killSetMap = new HashMap<Statement, Set<SimpleName>>();
    genSetMap = new HashMap<Statement, Set<SimpleName>>();
    
    parameterDefinitions = createParameterDefinitions(cfg.getMethodDeclaration());
    computeKillAndGenSets();
    buildEntrySetMap();
    
    return new ReachingDefinitions() {
      final Map<Statement, Set<Definition>> reachingDefinitions = 
          Collections.unmodifiableMap(entrySetMap);

      @Override 
      public Set<Definition> getReachingDefinitions(final Statement s) {
        Set<Definition> returnValue = null;
        if (reachingDefinitions.containsKey(s)) {
          returnValue = reachingDefinitions.get(s);
        }
        return returnValue;
      }
    };
  }

  private Set<Definition> createParameterDefinitions(MethodDeclaration methodDeclaration) {
    List<VariableDeclaration> parameterList = 
        getParameterList(methodDeclaration.parameters());
    Set<Definition> set = new HashSet<Definition>();

    for (VariableDeclaration parameter : parameterList) {
      Definition definition = createDefinition(parameter.getName(), null);
      set.add(definition);  
    }

    return set;
  }

  /**
   * Computes the kill and gen sets for each statement in a cfg. These sets are
   * not modified once computed. 
   *
   * @modifies killSetMap and genSetMap
   */
  private void computeKillAndGenSets() {
    Statement start = cfg.getStart();
    Set<Statement> visited = new HashSet<>();
    computeKillAndGenSetsHelper(start, visited);
  }

  /**
   * Computes the entry sets for all statements in a method.
   *
   * @modifies exitSetMap, entrySetMap
   */
  private void buildEntrySetMap() {
    Statement start = cfg.getStart();
    LinkedList<Statement> workList = new LinkedList<>();
    workList.add(start);
    
    // WorkList algorithm
    while (!workList.isEmpty()) {
      Statement n = workList.removeFirst();

      // save oldExitSet for later reference
      Set<Definition> oldExitSet = null;
      if (exitSetMap.containsKey(n)) {
        oldExitSet = exitSetMap.get(n);
      }

      // compute entry set for n
      Set<Definition> newEntrySet = computeEntrySet(n);
      entrySetMap.put(n, newEntrySet);

      // compute new exit set for n
      Set<Definition> newExitSet = new HashSet<>(newEntrySet);
      subtractKillSet(n, newExitSet);
      unionGenSet(n, newExitSet);
      exitSetMap.put(n, newExitSet);

      // add successors to workList if exitSet changed
      if (oldExitSet == null || !oldExitSet.equals(newExitSet)) {
        Set<Statement> succs = cfg.getSuccs(n);
        if (succs != null) {
          for (Statement succ : succs) {
            workList.add(succ);
          }
        }
      }
    }
  }

  /**
   * Recursive helper to compute the kill and gen sets for each statement in a
   * cfg.
   *
   * @modifies killSetMap and genSetMap.
   * @param n The current statement for which to generate kill and gen sets.
   * @param visited The statements that have already been visited, as a set.
   */
  private void computeKillAndGenSetsHelper(Statement n, Set<Statement> visited) {
    Set<SimpleName> killSet = new HashSet<SimpleName>();
    Set<SimpleName> genSet = new HashSet<SimpleName>();
    visited.add(n);

    // compute kill and gen sets
    if (n instanceof ExpressionStatement) {
      // is ExpressionStatement
      ExpressionStatement exp = (ExpressionStatement) n;
      if (exp.getExpression() instanceof Assignment) {
        // is Assignment (case 1)
        Assignment assignment = (Assignment) exp.getExpression();
        SimpleName name = (SimpleName) assignment.getLeftHandSide();
        killSet.add(name);
        genSet.add(name);
      }
    } else if (n instanceof VariableDeclarationStatement) {
      // is VariableDeclarationStatement (case 2)
      List<VariableDeclarationFragment> varDeclFragList =
          getVariableDeclarationFragmentList(((VariableDeclarationStatement) n).fragments());
      VariableDeclarationFragment varDeclFrag = varDeclFragList.get(0);
      SimpleName name = varDeclFrag.getName();
      killSet.add(name);
      genSet.add(name);
    }
    killSetMap.put(n, killSet);
    genSetMap.put(n, genSet);

    // recursively visit successors
    Set<Statement> successorSet = cfg.getSuccs(n);
    if (successorSet != null) {
      for (Statement succ : successorSet) {
        if (!visited.contains(succ)) {
          computeKillAndGenSetsHelper(succ, visited);
        }
      }
    }
  }

  private Set<Definition> computeEntrySet(Statement n) {
    Set<Definition> newEntrySet;
    if (n == cfg.getStart()) {
      // entry set for first statement contains params (set previously)
      newEntrySet = new HashSet<>(parameterDefinitions);
    } else {
      newEntrySet = new HashSet<>();
    }

    // entry set is union of exit sets of all predecessors
    Set<Statement> preds = cfg.getPreds(n);
    if (preds != null) {
      for (Statement pred : preds) {
        if (exitSetMap.containsKey(pred)) {
          Set<Definition> predExitSet = exitSetMap.get(pred);
          // union pred's defs to n's entry set
          for (Definition predExitDef : predExitSet) {
            newEntrySet.add(predExitDef);
          }
        }
      }
    }

    return newEntrySet;
  }

  private void subtractKillSet(Statement n, Set<Definition> newExitSet) {
    Set<SimpleName> killSet = killSetMap.get(n);
    for (SimpleName name : killSet) {
      newExitSet.removeIf(definition -> 
          definition.name.getIdentifier().equals(name.getIdentifier()));
    }
  }

  private void unionGenSet(Statement n, Set<Definition> newExitSet) {
    Set<SimpleName> genSet = genSetMap.get(n);
    for (SimpleName genName : genSet) {
      // add a definition for each item in the gen set
      Definition definition = createDefinition(genName, n);
      newExitSet.add(definition);
    }
  }

  private Definition createDefinition(SimpleName name, Statement statement) {
    Definition definition = new Definition();
    definition.name = name;
    definition.statement = statement;
    return definition;
  }

  private List<VariableDeclaration> getParameterList(Object list) {
    @SuppressWarnings("unchecked")
    List<VariableDeclaration> statementList = (List<VariableDeclaration>) (list);
    return statementList;
  }

  private List<VariableDeclarationFragment> getVariableDeclarationFragmentList(Object list) {
    @SuppressWarnings("unchecked")
    List<VariableDeclarationFragment> varDeclList = (List<VariableDeclarationFragment>) (list);
    return varDeclList;
  }
}
