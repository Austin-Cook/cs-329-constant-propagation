package cfgInputs;

public class should_LinkAll_when_ExistsElseBlock {
    void name() {
        int i;

        if (true) {
            i = 1;
            i = 2;
        } else {
            i = 4;
            i = 5;
        }
        // no following variable
    }
}
