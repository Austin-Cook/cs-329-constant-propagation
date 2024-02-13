package rdInputs;

public class AssignBranch {
    public void name(int a) {
        if (true) {     // (a, *)
            a = 1;      // (a, *)
        } else {
            a = 2;      // (a, *)
        }
    }                   // (a, "a = 1"), (a, "a = 2")
}
