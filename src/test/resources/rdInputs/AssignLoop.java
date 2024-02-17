package rdInputs;

public class AssignLoop {
    public name(int a) {
        while (true) {  // (a, *), (a, "a = 2")
            a = 1;      // (a, *), (a, "a = 2")
            a = 2;      // (a, "a = 1")
        }
    }                   // (a, *), (a, "a = 2")
}
