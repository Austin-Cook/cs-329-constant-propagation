package edu.byu.cs329.constantpropagation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import edu.byu.cs329.TestUtils;

@DisplayName("Tests for ConstantPropagation")
public class ConstantPropagationTests {
    @Nested
    class BlackBoxTests {
        @Test
        @Tag("NoChange")
        @DisplayName("Should not propagate anything when there is nothing to propagate")
        public void should_NotPropagate_when_ThereIsNothingToPropagate() {
            String rootName = "constantPropagationInputs/should_NotPropagate_when_ThereIsNothingToPropagate-root.java";
            String expectedName = "constantPropagationInputs/should_NotPropagate_when_ThereIsNothingToPropagate-root.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }

        @Test
        @Tag("NoChange")
        @DisplayName("Should not propagate when there are two definitions")
        public void should_NotPropagate_when_ThereAreTwoDefinitions() {
            String rootName = "constantPropagationInputs/should_NotPropagate_when_ThereAreTwoDefinitions-root.java";
            String expectedName = "constantPropagationInputs/should_NotPropagate_when_ThereAreTwoDefinitions-root.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }

        @Test
        @Tag("NoChange")
        @DisplayName("Should not propagate when variable is to the left of the assign")
        public void should_NotPropagate_when_VariableIsLeftOfAssign() {
            String rootName = "constantPropagationInputs/should_NotPropagate_when_VariableIsLeftOfAssign-root.java";
            String expectedName = "constantPropagationInputs/should_NotPropagate_when_VariableIsLeftOfAssign-root.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }

        @Test
        @Tag("Change")
        @DisplayName("Should propagate when there is one definition")
        public void should_Propagate_when_ThereIsOneDefinition() {
            String rootName = "constantPropagationInputs/should_Propagate_when_ThereIsOneDefinition-root.java";
            String expectedName = "constantPropagationInputs/should_Propagate_when_ThereIsOneDefinition.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }

        @Test
        @Tag("Change")
        @DisplayName("Should propagate when there are multiple levels to propegate and no folding")
        public void should_Propagate_when_MultipleLevelsToPropegateAndNoFolding() {
            String rootName = "constantPropagationInputs/should_Propagate_when_MultipleLevelsToPropegateAndNoFolding-root.java";
            String expectedName = "constantPropagationInputs/should_Propagate_when_MultipleLevelsToPropegateAndNoFolding.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }

        @Test
        @Tag("Change")
        @DisplayName("Should propagate when there are multiple levels to propegate and folding")
        public void should_Propagate_when_MultipleLevelsToPropegateAndFolding() {
            String rootName = "constantPropagationInputs/should_Propagate_when_MultipleLevelsToPropegateAndFolding-root.java";
            String expectedName = "constantPropagationInputs/should_Propagate_when_MultipleLevelsToPropegateAndFolding.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }
    }
    
    @Nested
    class WhiteBoxTests {
        @Test
        @Tag("WhiteBox")
        @DisplayName("Should not throw exception when create instance of constant propagation")
        public void should_NotThrowException_when_CreateInstanceOfConstantPropagation() {
            assertDoesNotThrow(() -> {
                new ConstantPropagation();
            });
        }

        @Test
        @Tag("WhiteBox")
        @Tag("Main")
        @DisplayName("Should not throw exception when main run with invalid number of args")
        public void should_NotThrowException_when_MainRunWithInvalidNumberOfArgs() {
            String[] args = new String[1];
            args[0] = "src/test/resources/constantPropagationInputs/should_NotPropagate_when_ThereIsNothingToPropagate-root.java";
            ConstantPropagation.main(args);
            assertDoesNotThrow(() -> {
                ConstantPropagation.main(args);
            });
        }

        @Test
        @Tag("WhiteBox")
        @Tag("Main")
        @DisplayName("Should not throw exception when main run and second arg is null")
        public void should_NotThrowException_when_MainRunAndSecondArgIsNull() {
            String[] args = new String[2];
            args[0] = "src/test/resources/constantPropagationInputs/should_NotPropagate_when_ThereIsNothingToPropagate-root.java";
            args[1] = null;
            assertDoesNotThrow(() -> {
                ConstantPropagation.main(args);
            });
        }

        @Test
        @Tag("WhiteBox")
        @Tag("Main")
        @DisplayName("Should not propagate when there is nothing to propagate and run from main")
        public void should_NotPropagate_when_ThereIsNothingToPropagateAndRunFromMain() {
            String root = "constantPropagationInputs/should_NotPropagate_when_ThereIsNothingToPropagate-root.java";
            String actual = "out/should_NotPropagate_when_ThereIsNothingToPropagate.java";
            String[] args = new String[2];
            args[0] = "src/test/resources/" + root;
            args[1] = "src/test/resources/" + actual;
            ConstantPropagation.main(args);
            TestUtils.assertSubtreesEqual(this, root, actual);
        }
        
        @Test
        @Tag("WhiteBox")
        @DisplayName("Should propagate when InfixOperator and no extended operands")
        public void should_Propagate_when_InfixOperatorAndNoExtendedOperands() {
            String rootName = "constantPropagationInputs/whiteBox/should_Propagate_when_InfixOperatorAndNoExtendedOperands-root.java";
            String expectedName = "constantPropagationInputs/whiteBox/should_Propagate_when_InfixOperatorAndNoExtendedOperands.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }

        @Test
        @Tag("WhiteBox")
        @DisplayName("Should not propagate when literal not number or boolean")
        public void should_NotPropagate_when_LiteralNotNumberOrBoolean() {
            String rootName = "constantPropagationInputs/whiteBox/should_NotPropagate_when_LiteralNotNumberOrBoolean-root.java";
            String expectedName = "constantPropagationInputs/whiteBox/should_NotPropagate_when_LiteralNotNumberOrBoolean-root.java";
            TestUtils.assertEquals_ConstantPropagation(this, rootName, expectedName);
        }
    }
}
