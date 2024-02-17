package rdInputs;

public class AssignLoopReturn {
    public void name(int a) {
        while (true) {  // (a, *)
            a = 1;      // (a, *)
            return;     // (a, "a = 1")
        }
        boolean b;      // (a, *)
    }                   // (a, *), (a, "a = 1"), (b, "boolean b")
}
