package cfgInputs;

public class should_NotLinkFromReturn_when_NoElseBlockAndThenBlockReturns {
    void name() {
        int i;

        if (true) {
            i = 1;
            return;
        }
        i = 3;
    }
}
