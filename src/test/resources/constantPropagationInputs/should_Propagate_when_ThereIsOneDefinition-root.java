public class Name {
    public int name() {
        boolean b = true;
        boolean b2 = b;

        char c = 'c';
        char c2 = c;

        Integer nl = null;
        Integer nl2 = nl;

        String s = "str";
        String s2 = s;

        Class<?> t = String.class;
        Class<?> t2 = t;

        int n = 1;
        int n2 = n;

        if (b) {}
        while (b) {}
        do {} while (b);
        b2 = !b;
        n2 = (n);
        n2 = n;
        int n3 = n + n + n2;
        return n;
    }
}