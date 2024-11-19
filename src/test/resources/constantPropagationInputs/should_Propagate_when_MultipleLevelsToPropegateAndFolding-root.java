public class Name {
    public void name() {
        int n;
        if (true) {
            n = (1);
        } else {
            n = 2;
        }

        int n2 = (n + 1);           // n2 = 2
        boolean b = (n2 < 10);      // b = true
        boolean b2 = !b;            // b2 = false
        int n3 = n + n2;            // n3 = 3;

        while (n2 < 10) {
            n2 = n2 + 1;            // doesn't propagate
        }
    }
}