public class Name {
    public int name() {
        boolean b = true;
        boolean b2 = b;

        int n = 1;
        int n2 = n;

        if (b) {}
        while (b) {}
        do {} while (b);
        b2 = !b;
        n2 = (n);
        n2 = n;
        int n3 = n + n + n2;
        // boolean b3 = b && b2 && b2;
        return n;
    }
}