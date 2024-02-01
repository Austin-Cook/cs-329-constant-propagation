public class Name {
    public void name() {
        int i = 2;

        if (!(3 < 1)) {
            if (true) {
                i = 5;
            }
        } else {
            i = 1;
        }

        if (!((1 < 3))) {
            i = 1;
        } else {
            if (!true) {
                i = 1;
            }
            i = 5;
        }

        if (!((1 + 2 + 3) < (10))) { // should be 10
            i = 1;
        }

        if (true) {
            i = 5;
        }
    }
}