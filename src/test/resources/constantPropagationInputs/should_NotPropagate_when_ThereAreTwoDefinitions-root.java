public class Name {
    public void name(boolean a) {
        boolean b;
        char c;
        Integer nl;
        String s;
        Class<?> t;
        int n;

        if (a) {
            b = true;
            c = 'c';
            nl = null;
            s = "str";
            t = String.class;
            n = 1;
        } else {
            b = false;
            c = 'd';
            nl = null;
            s = "str2";
            t = String.class;
            n = 2;
        }

        boolean b2 = b;
        char c2 = c;
        Integer nl2 = nl;
        String s2 = s;
        Class<?> t2 = t;
        int n2 = n;
    }
}