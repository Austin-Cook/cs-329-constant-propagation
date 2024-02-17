package rdInputs;

public class AssignBranchReturn {
    public name(int a) {
        if (true) {     // (a, *)
            a = 1;      // (a, *)
            return;     // (a, "A = 1")
        } else {
            a = 2;      // (a, *)
        }
        boolean b;      // (a, "a = 2")
    }                   // (a, "a = 1"), (a, "a = 2"), (b, "boolean b")
}
