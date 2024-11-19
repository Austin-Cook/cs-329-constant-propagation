public class Name {
	int name(int i) {
		boolean a = 1 < 3; // true
		boolean b = 1 + 2 < 3 - 4; // should not run
		boolean c = 1 < 7 + 8; // should not run
		boolean d = 5 > 2; // '>' should not run
	}
}
