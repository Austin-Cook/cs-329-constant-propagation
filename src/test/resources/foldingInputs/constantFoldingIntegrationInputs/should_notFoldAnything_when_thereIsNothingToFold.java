public class Name {
  public void name() {
    int i = 2;
    bool b = true;

    bool c = (b);     // paren
    c = !(b);         // negation & param
    c = !b;           // negation
    c = !!!!!b;       // negation

    int d = (i + 1);  // paren & plus
    d = 1 + 2 + i;    // plus
    d = i + i;        // plus

    c = 1 < i;        // less than
    c = i < 1;        // less than

    if (((((b))))) {  // paren & if
      c = true;
    } else {
      c = false;
    }

    if (b) {          // paren
      c = true;
    }

    if (!b) {         // paren and not
      c = false;
    }
  }
}
