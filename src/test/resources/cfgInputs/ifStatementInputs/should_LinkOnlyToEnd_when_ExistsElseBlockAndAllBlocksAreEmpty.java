package cfgInputs;

public class should_LinkOnlyToEnd_when_ExistsElseBlockAndAllBlocksAreEmpty {
    void name() {
        int i;

        if (true) {} 
        else {}
        i = 3;
    }
}
