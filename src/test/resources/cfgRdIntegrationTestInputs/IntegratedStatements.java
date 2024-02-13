public class Name {
    public void name(int a) {
        while (true) {  // (a, *), (a, "a = 2")
            a = 1;      // (a, *), (a, "a = 2")
            a = 2;      // (a, 1)
        }

        boolean b;      // (a, *), (a, "a = 2")

        if (true) {     // (a, *), (a, "a = 2"), (b, "boolean b")
            a = 3;      // (a, *), (a, "a = 2"), (b, "boolean b")
        } else {
            a = 4;      // (a, *), (a, "a = 2"), (b, "boolean b")
            return;     // (a, "a = 4"), (b, "boolean b")
        }
    }                   // (a, "a = 3"), (a, "a = 4"), (b, "boolean b")
}
