public class Name {
    public void name() {
        // paren
        boolean b = (true);
        b = (false);
        b = ((true));
        b = ((false));
        final char c = ('c');
        final Integer i = (null);
        final int x = (((3)));
        final String s = new String(("Hello"));
        final boolean t = ((Name.class) == Name.class);

        // negation
        b = !true;
        b = !false;
        b = !!true;
        b = !!false;

        // plus
        int a = 1 + 2;
        a = 1 + 2 + 3;
        a = 1 + 2 + 3 + 4;
        
        // lesss than
        b = 1 < 3;
        b = 3 < 1;

        // if
        if (true) {
            a = 1;
        } else {
            a = 2;
        }
        if (false) {
            a = 3;
        } else {
            a = 4;
        }
        if (false) {
            a = 5;
        }
        a = 6;
    }
}
