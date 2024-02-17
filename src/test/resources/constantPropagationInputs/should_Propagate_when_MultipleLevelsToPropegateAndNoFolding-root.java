public class Name {
    public void name() {
        boolean b = true;
        boolean b2 = b;
        boolean b3 = b2;

        char c = 'c';
        char c2 = c;
        char c3 = c2;

        Integer nl = null;
        Integer nl2 = nl;
        Integer nl3 = nl2;

        String s = "str";
        String s2 = s;
        String s3 = s2;

        Class<?> t = String.class;
        Class<?> t2 = t;
        Class<?> t3 = t2;

        int n = 1;
        int n2 = n;
        int n3 = n2;
    }
}