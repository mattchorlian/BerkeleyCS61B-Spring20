package enigma;

import static enigma.EnigmaException.*;
import java.util.ArrayList;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Matt Chorlian
 */
class Permutation {

    /** Alphabet used for this permutation. */
    private Alphabet _alphabet;

    /** size of current alphabet. */
    private int _size;

    /** Cycles used for this permutation. */
    private ArrayList<Object> cyclelist;



    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _size = _alphabet.size();
        cyclelist = new ArrayList<>();
        for (int i = 0; i < _size; i++) {
            cyclelist.add(i);
        }

        String[] stringarr;
        cycles = cycles.replaceAll("[)(]", "");
        stringarr = cycles.split(" ");
        for (String c: stringarr) {
            addCycle(c);
        }



    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        for (int i = 0; i < cycle.length(); i++) {
            char curr = cycle.charAt(i);
            if (cycle.length() == 0) {
                cyclelist.set(_alphabet.toInt(curr), _alphabet.toInt(curr));
            } else if (i != (cycle.length() - 1)) {
                char next = cycle.charAt(i + 1);
                cyclelist.set(_alphabet.toInt(curr), _alphabet.toInt(next));
            } else {
                char first = cycle.charAt(0);
                cyclelist.set(_alphabet.toInt(curr), _alphabet.toInt(first));
            }

        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        return (int) cyclelist.get(wrap(p));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        return cyclelist.indexOf(wrap(c));
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int index =  _alphabet.toInt(p);
        return _alphabet.toChar(permute(index));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int index = _alphabet.toInt(c);
        return _alphabet.toChar(invert(index));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (Object i : cyclelist) {
            if (i.equals(cyclelist.indexOf(i))) {
                return false;
            }
        }
        return true;
    }
}
