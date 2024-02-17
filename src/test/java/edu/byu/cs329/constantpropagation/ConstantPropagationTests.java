package edu.byu.cs329.constantpropagation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import edu.byu.cs329.TestUtils;

@DisplayName("Tests for ConstantPropagation")
public class ConstantPropagationTests {
    @Nested
    class BlackBoxTests {
        // nothing to fold
        @Test
        @Tag("")
        @DisplayName("Should not propagate anything when there is nothing to propagate")
        public void should_NotPropagate_when_ThereIsNothingToPropagate() {
            String rootName = "constantPropagationInputs/should_NotPropagate_when_ThereIsNothingToPropagate-root.java";
            String expectedName = "constantPropagationInputs/should_NotPropagate_when_ThereIsNothingToPropagate-root.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }

        // 1rd
        @Test
        @Tag("")
        @DisplayName("Should propagate when there is one definition")
        public void should_Propagate_when_ThereIsOneDefinition() {
            String rootName = "constantPropagationInputs/should_Propagate_when_ThereIsOneDefinition-root.java";
            String expectedName = "constantPropagationInputs/should_Propagate_when_ThereIsOneDefinition.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }

        // 2rd
        @Test
        @Tag("")
        @DisplayName("Should not propagate when there are two definitions")
        public void should_NotPropagate_when_ThereAreTwoDefinitions() {
            String rootName = "constantPropagationInputs/should_NotPropagate_when_ThereAreTwoDefinitions-root.java";
            String expectedName = "constantPropagationInputs/should_NotPropagate_when_ThereAreTwoDefinitions-root.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }

        // varleftassign
        @Test
        @Tag("")
        @DisplayName("Should not propagate when variable is to the left of the assign")
        public void should_NotPropagate_when_VariableIsLeftOfAssign() {
            String rootName = "constantPropagationInputs/should_NotPropagate_when_VariableIsLeftOfAssign-root.java";
            String expectedName = "constantPropagationInputs/should_NotPropagate_when_VariableIsLeftOfAssign-root.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }

        // chained - no folding
        @Test
        @Tag("")
        @DisplayName("Should propagate when there are multiple levels to propegate and no folding")
        public void should_Propagate_when_MultipleLevelsToPropegateAndNoFolding() {
            String rootName = "constantPropagationInputs/should_Propagate_when_MultipleLevelsToPropegateAndNoFolding-root.java";
            String expectedName = "constantPropagationInputs/should_Propagate_when_MultipleLevelsToPropegateAndNoFolding.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }

        // chained yes folding
        @Test
        @Tag("")
        @DisplayName("Should propagate when there are multiple levels to propegate and folding")
        public void should_Propagate_when_MultipleLevelsToPropegateAndFolding() {
            String rootName = "constantPropagationInputs/should_Propagate_when_MultipleLevelsToPropegateAndFolding-root.java";
            String expectedName = "constantPropagationInputs/should_Propagate_when_MultipleLevelsToPropegateAndFolding.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }
    }
    
    @Nested
    class WhiteBoxTests {
        // TODO
    }
    
    
    
}
