package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Matt Chorlian
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform1() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }


    @Test
    public void sizeTest() {
        Alphabet A = new Alphabet("ABCDEFG");
        Alphabet B = new Alphabet("DEFXAWUHNKOP");
        Alphabet C = new Alphabet();
        Alphabet D = new Alphabet("A");
        Alphabet E = new Alphabet("");
        assertEquals(7, A.size());
        assertEquals(12, B.size());
        assertEquals(26, C.size());
        assertEquals(1, D.size());
        assertEquals(0, E.size());
    }


    @Test
    public void permutecharTest() {
        Permutation p =
                new Permutation("(AELTPHQXRU) "
                        + "(BKNW) (CMOY) (DFG) (IV) (JZ) (S)",
                        new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        assertEquals('E', p.permute('A'));
        assertEquals('X', p.permute('Q'));
        assertEquals('B', p.permute('W'));
        assertEquals('M', p.permute('C'));
        assertEquals('F', p.permute('D'));
        assertEquals('G', p.permute('F'));
        assertEquals('I', p.permute('V'));
        assertEquals('V', p.permute('I'));
        assertEquals('S', p.permute('S'));
        assertEquals('J', p.permute('Z'));
        assertEquals('A', p.permute('U'));

    }

    @Test
    public void permuteintTest() {
        Permutation p1 =
                new Permutation("(FHNIGLR)",
                        new Alphabet("HILFNGR"));
        assertEquals(4, p1.permute(0));
        assertEquals(5, p1.permute(1));
        assertEquals(6, p1.permute(2));
        assertEquals(0, p1.permute(3));
        assertEquals(1, p1.permute(4));
        assertEquals(2, p1.permute(5));
        assertEquals(3, p1.permute(6));
    }

    @Test
    public void invertcharTest() {
        Permutation p =
                new Permutation("(AELTPHQXRU) "
                        + "(BKNW) (CMOY) (DFG) (IV) (JZ) (S)",
                        new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        assertEquals('A', p.invert('E'));
        assertEquals('S', p.invert('S'));
        assertEquals('B', p.invert('K'));
        assertEquals('W', p.invert('B'));
        assertEquals('C', p.invert('M'));
        assertEquals('Y', p.invert('C'));
        assertEquals('I', p.invert('V'));
        assertEquals('V', p.invert('I'));
        assertEquals('J', p.invert('Z'));
    }

    @Test
    public void invertintTest() {
        Permutation p1 =
                new Permutation("(FHNIGLR)",
                        new Alphabet("HILFNGR"));
        assertEquals(3, p1.invert(0));
        assertEquals(4, p1.invert(1));
        assertEquals(5, p1.invert(2));
        assertEquals(6, p1.invert(3));
        assertEquals(0, p1.invert(4));
        assertEquals(1, p1.invert(5));
        assertEquals(2, p1.invert(6));
    }

    @Test
    public void invertintTest2() {
        Permutation p =
                new Permutation("(A) (B) (C)",
                        new Alphabet("ABC"));
        assertEquals(0, p.invert(0));
        assertEquals(1, p.invert(1));
        assertEquals(2, p.permute(2));
        assertEquals(0, p.invert(p.invert(0)));
    }

    @Test
    public void alphabetTest() {
        Alphabet alpha1 = new Alphabet("ABCD");
        Alphabet alpha2 = new Alphabet("HILFNGR");
        Permutation p1 =  new Permutation("(BACD)", alpha1);
        Permutation p2 =  new Permutation("(HIG) (NF) (L)", alpha2);

        assertEquals(p1.alphabet(), alpha1);
        assertEquals(p2.alphabet(), alpha2);
        assertFalse(p1.alphabet() == new Alphabet("DABC"));
        assertFalse(p2.alphabet() == new Alphabet("HFGLINR"));
    }

    @Test
    public void alphabetmethodtests() {
        Alphabet a = new Alphabet();
        assertTrue(a.contains('A'));
        assertFalse(a.contains('6'));
    }

    @Test
    public void derangementTest() {
        Permutation p1 =
                new Permutation("(HIG) (NF) (L)",
                        new Alphabet("HILFNGR"));
        Permutation p2 =  new Permutation("(BACD)", new Alphabet("ABCD"));
        Permutation p3 = new Permutation("(ABC)", new Alphabet("ABCD"));
        assertFalse(p1.derangement());
        assertTrue(p2.derangement());
        assertFalse(p3.derangement());
    }

    @Test
    public void permutationTest() {
        Permutation p =
                new Permutation("(DEFGACB)",
                        new Alphabet("ABCDEFG"));
        assertEquals(p.size(), 7);
        assertTrue(p.derangement());
        assertEquals('E', p.permute('D'));
        assertEquals('D', p.invert('E'));
        assertEquals(0, p.invert(2));
        assertEquals(4, p.permute(3));
    }

    @Test
    public void extendedpermutationTest() {
        Alphabet alphab = new Alphabet();
        Permutation p = new Permutation("(AELTPHQXRU) "
               + "(BKNW) (CMOY) (DFG) (IV) (JZ) (S)", alphab);
        assertEquals(p.size(), 26);
        assertFalse(p.derangement());
        assertEquals('W', p.permute('N'));
        assertEquals('U', p.invert('A'));
        assertEquals(5, p.permute(3));
        assertEquals(25, p.invert(9));
        assertEquals(0, alphab.toInt('A'));
        assertEquals('F', alphab.toChar(5));
        assertFalse(alphab.contains('5'));

    }

    @Test
    public void edgetest() {
        Permutation p = new Permutation("(D)", new Alphabet("ABCDEFG"));
        assertEquals(p.permute('A'), p.invert('A'));
        assertEquals(p.invert(3), p.permute(3));
        assertEquals(p.invert(p.invert(p.invert('A'))), 'A');

    }

    @Test(expected = EnigmaException.class)
    public void exceptiontest1() {
        Permutation p = new Permutation("(BACD)", new Alphabet("ABCD"));
        p.invert('F');
        p.invert(10);
        p.invert(-10);
        p.invert(p.size());
    }

    @Test(expected = EnigmaException.class)
    public void exceptiontest2() {
        Permutation p = new Permutation("(BACD)", new Alphabet("ABCD"));
        p.permute('F');
        p.permute(10);
        p.permute(-10);
        p.permute(p.size());
    }


}
