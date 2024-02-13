package cfgInputs.whileStatementInputs;

public class should_NotLinkFromReturn_when_WhileBlockReturns {
    void name(int i) {
        while(true) {
            i = 1;
            return;
        }
    }
}
