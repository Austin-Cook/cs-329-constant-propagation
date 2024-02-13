package edu.byu.cs329.rd;

import java.util.Set;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Interface for reaching definitions.
 */
public interface ReachingDefinitions {
  
  /**
   * Definition entity.
   */
  public static class Definition { 
    public SimpleName name;
    public Statement statement;

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      
      if (this == obj) {
        return true;
      }

      if (this.getClass() != obj.getClass()) {
        return false;
      }

      Definition that = (Definition) obj;
  
      return ((this.name.getIdentifier().equals(that.name.getIdentifier())) 
          && (this.statement == that.statement));
    }

    @Override
    public int hashCode() {
      int statementHash = 29;
      if (this.statement != null) {
        statementHash = this.statement.hashCode();
      }

      return this.name.hashCode() + statementHash;
    }

    @Override
    public String toString() {
      String statementString;
      if (statement == null) {
        statementString = "*";
      } else {
        statementString = statement.toString();
      }
      
      return "(" + name.getIdentifier() + ", " + statementString + ")";
    }
  }
  
  public Set<Definition> getReachingDefinitions(final Statement s);
}

