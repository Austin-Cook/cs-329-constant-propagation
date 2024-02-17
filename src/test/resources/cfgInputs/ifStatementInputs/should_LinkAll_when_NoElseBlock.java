package cfgInputs;

public class should_LinkAll_when_NoElseBlock {
    void name() {
        int i;

        if (true) {
            i = 1;
            i = 2;
        }
        i = 3;
    }
}
