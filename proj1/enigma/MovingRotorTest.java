package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Matt Chorlian
 */
public class MovingRotorTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Rotor rotor;
    private String alphab = UPPER_STRING;

    /** Check that rotor has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkRotor(String testId,
                            String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, rotor.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            int ci = alphab.indexOf(c), ei = alphab.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d (%c)", ci, c),
                         ei, rotor.convertForward(ci));
            assertEquals(msg(testId, "wrong inverse of %d (%c)", ei, e),
                         ci, rotor.convertBackward(ei));
        }
    }

    /** Set the rotor to the one with given NAME and permutation as
     *  specified by the NAME entry in ROTORS, with given NOTCHES. */
    private void setRotor(String name, HashMap<String, String> rotors,
                          String notches) {
        rotor = new MovingRotor(name, new Permutation(rotors.get(name), UPPER),
                                notches);
    }

    /* ***** TESTS ***** */

    @Test
    public void checkRotorAtA() {
        setRotor("I", NAVALA, "");
        checkRotor("Rotor I (A)", UPPER_STRING, NAVALA_MAP.get("I"));
    }

    @Test
    public void checkRotorAdvance() {
        setRotor("I", NAVALA, "");
        rotor.advance();
        checkRotor("Rotor I advanced", UPPER_STRING, NAVALB_MAP.get("I"));
    }

    @Test
    public void checkRotorSet() {
        setRotor("I", NAVALA, "");
        rotor.set(25);
        checkRotor("Rotor I set", UPPER_STRING, NAVALZ_MAP.get("I"));
    }

    @Test
    public void simpleconvertTest() {
        Alphabet alpha = new Alphabet("ABCD");
        Rotor B =  new Reflector("Reflector B",
                new Permutation("(A) (B) (C) (D)", alpha));
        Rotor iII = new MovingRotor("Rotor III",
                new Permutation("(ABCD)", alpha), "C");
        Rotor iI = new MovingRotor("Rotor II",
                new Permutation("(ABCD)", alpha), "C");
        Rotor I = new MovingRotor("Rotor I",
                new Permutation("(ABCD)", alpha), "C");
        String setting = "AAAA";
        Rotor[] rotors = {B, iII, iI, I};
        String[] rN = {"Reflector B", "Rotor III", "Rotor II", "Rotor I"};
        ArrayList<Rotor> temp = new ArrayList<>(Arrays.asList(rotors));
        Machine machine = new Machine(alpha, 4, 3, temp);
        machine.insertRotors(rN);
        machine.setRotors(setting);

        assertEquals("A", machine.convert("A"));
        assertEquals(alpha.toInt('B'), I.setting());
        assertEquals("A", machine.convert("A"));
        assertEquals(alpha.toInt('C'), I.setting());
        assertEquals("A", machine.convert("A"));
        assertEquals(alpha.toInt('D'), I.setting());
        assertEquals(alpha.toInt('B'), iI.setting());

        machine.setRotors(setting);
        assertEquals("AAA", machine.convert("AAA"));
        assertEquals(3, I.setting());
        assertEquals(1, iI.setting());
    }


    @Test
    public void doubleStepTest() {
        Alphabet alpha = new Alphabet();
        Rotor B =  new Reflector("Reflector B",
                new Permutation("(AE) (BN) (CK) (DQ) (FU) (GY) "
                        + "(HW) (IJ) (LO) (MP) (RX) (SZ) (TV)", alpha));
        Rotor iII = new MovingRotor("Rotor III",
                new Permutation("(ABDHPEJT) "
                        + "(CFLVMZOYQIRWUKXSG) (N)", alpha), "V");
        Rotor iI = new MovingRotor("Rotor II",
                new Permutation("(FIXVYOMW) (CDKLHUP) "
                        + "(ESZ) (BJ) (GR) (NT) (A) (Q)", alpha), "E");
        Rotor I = new MovingRotor("Rotor I",
                new Permutation("(AELTPHQXRU) (BKNW) "
                        + "(CMOY) (DFG) (IV) (JZ) (S)", alpha), "Q");
        String setting = "AAAA";
        Rotor[] rotors = {B, iII, iI, I};
        String[] rN = {"Reflector B", "Rotor III", "Rotor II", "Rotor I"};
        ArrayList<Rotor> temp = new ArrayList<>(Arrays.asList(rotors));
        Machine machine = new Machine(alpha, 4, 3, temp);
        machine.insertRotors(rN);
        machine.setRotors(setting);

        machine.convert("MMMMMMMMMMMMMMMMM");
        assertEquals(alpha.toInt('R'), I.setting());
        assertEquals(1, iI.setting());
    }

    @Test(expected = EnigmaException.class)
    public void checknoReflector() {
        Alphabet alpha = new Alphabet();
        Rotor iII = new MovingRotor("Rotor III",
                new Permutation("(ABDHPEJT) "
                        + "(CFLVMZOYQIRWUKXSG) (N)", alpha), "V");
        Rotor iI = new MovingRotor("Rotor II",
                new Permutation("(FIXVYOMW) (CDKLHUP) (ESZ) "
                        + "(BJ) (GR) (NT) (A) (Q)", alpha), "E");
        Rotor I = new MovingRotor("Rotor I",
                new Permutation("(AELTPHQXRU) (BKNW) "
                        + "(CMOY) (DFG) (IV) (JZ) (S)", alpha), "Q");
        String setting = "AAA";
        Rotor[] rotors = {iII, iI, I};
        String[] rotorNames = {"Rotor III", "Rotor II", "Rotor I"};
        ArrayList<Rotor> temp = new ArrayList<>(Arrays.asList(rotors));
        Machine machine = new Machine(alpha, 3, 3, temp);
        machine.insertRotors(rotorNames);
        machine.setRotors(setting);
    }

    @Test(expected = EnigmaException.class)
    public void checkduplicateRotor() {
        Alphabet alpha = new Alphabet();
        Rotor B =  new Reflector("Reflector B",
                new Permutation("(AE) (BN) (CK) (DQ) (FU) (GY) "
                        + "(HW) (IJ) (LO) (MP) (RX) (SZ) (TV)", alpha));
        Rotor iII = new MovingRotor("Rotor III",
                new Permutation("(ABDHPEJT) "
                        + "(CFLVMZOYQIRWUKXSG) (N)", alpha), "V");
        Rotor iI = new MovingRotor("Rotor II",
                new Permutation("(FIXVYOMW) (CDKLHUP) "
                        + "(ESZ) (BJ) (GR) (NT) (A) (Q)", alpha), "E");
        Rotor I = new MovingRotor("Rotor I",
                new Permutation("(AELTPHQXRU) (BKNW) "
                        + "(CMOY) (DFG) (IV) (JZ) (S)", alpha), "Q");
        String setting = "AAAA";
        Rotor[] rotors = {B, iII, iI, I, I};
        String t = "Reflector B";
        String[] rN = {t, "Rotor III", "Rotor II", "Rotor I", "Rotor I"};
        ArrayList<Rotor> temp = new ArrayList<>(Arrays.asList(rotors));
        Machine machine = new Machine(alpha, 5, 4, temp);
        machine.insertRotors(rN);
        machine.setRotors(setting);
    }

    @Test
    public void trivial1Test() {
        Alphabet alpha = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Rotor B =  new Reflector("Reflector B",
                new Permutation("(AE) (BN) (CK) (DQ) (FU)"
                        + " (GY) (HW) (IJ) (LO) (MP) (RX) (SZ) (TV)", alpha));
        Rotor beta = new FixedRotor("Rotor Beta",
                new Permutation("(ALBEVFCYODJWUGNMQTZSKPR)"
                        + " (HIX)", alpha));
        Rotor I = new MovingRotor("Rotor I",
                new Permutation("(AELTPHQXRU) (BKNW) "
                        + "(CMOY) (DFG) (IV) (JZ) (S)", alpha), "Q");
        Rotor iI = new MovingRotor("Rotor II",
                new Permutation("(FIXVYOMW) (CDKLHUP)"
                        + " (ESZ) (BJ) (GR) (NT) (A) (Q)", alpha), "E");
        Rotor iII = new MovingRotor("Rotor III",
                new Permutation("(ABDHPEJT) "
                        + "(CFLVMZOYQIRWUKXSG) (N)", alpha), "V");
        Rotor iV = new MovingRotor("Rotor IV",
                new Permutation("(AEPLIYWCOXMRFZBSTGJQNH)"
                        + " (DV) (KU)", alpha), "J");

        String setting = "AAAA";
        Rotor[] rotors = {B, beta, I, iI, iII, iV};
        String t = "Reflector B";
        String[] rN = {t, "Rotor Beta", "Rotor I", "Rotor II", "Rotor III"};
        ArrayList<Rotor> temp = new ArrayList<>(Arrays.asList(rotors));
        Machine machine = new Machine(alpha, 5, 3, temp);
        machine.insertRotors(rN);
        machine.setRotors(setting);

        assertEquals("ILBDAAMTAZ", machine.convert("HELLOWORLD"));
    }

}
