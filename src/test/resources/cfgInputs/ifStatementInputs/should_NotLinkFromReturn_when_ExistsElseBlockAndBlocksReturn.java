package cfgInputs;

public class should_NotLinkFromReturn_when_ExistsElseBlockAndBlocksReturn {
    void name() {
        int i;

        if (true) {
            i = 1;
            return;
        } else {
            i = 4;
            return;
        }
        i = 3;
    }
}
