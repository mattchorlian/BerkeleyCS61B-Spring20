package enigma;

import java.util.ArrayList;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Matt Chorlian
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _numPawls = pawls;
        _allRotors = new Rotor[allRotors.size()];
        int i = 0;
        for (Rotor r : allRotors) {
            _allRotors[i] = r;
            i += 1;
        }

    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numPawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {

        _currRotors = new ArrayList<Rotor>();
        for (int i = 0; i < rotors.length; i++) {
            for (Rotor r : _allRotors) {
                String rname = r.name().toUpperCase();
                if ((rname).equals((rotors[i].toUpperCase()))) {
                    _currRotors.add(r);
                }
            }
        }

        for (int i = 0; i < _currRotors.size(); i++) {
            for (int j = 0; j < _currRotors.size(); j++) {
                if (i != j) {
                    if (_currRotors.get(i) == _currRotors.get(j)) {
                        throw new EnigmaException("Dupicates Rotors");
                    } else {
                        continue;
                    }
                }
            }
        }
        int count = 0;
        for (int i = 0; i < _currRotors.size(); i++) {
            if (_currRotors.get(i).rotates()) {
                count += 1;
            }
        }
        if (count != numPawls()) {
            throw new EnigmaException("bad input");
        }

        if (!_currRotors.get(0).reflecting()) {
            throw new EnigmaException("First Rotor needs to be a reflector");
        } else if (_currRotors.size() != rotors.length) {
            throw new EnigmaException("Misnamed Rotors during insertion");
        }

    }



    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        for (int i = 1; i < _numRotors; i++) {
            _currRotors.get(i).set(setting.charAt(i - 1));
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Helper function to determine double stepping. **/
    void doublestepHelper() {
        boolean[] doublestep = new boolean[numRotors()];
        for (int i = 0; i < numRotors(); i++) {
            if (i == numRotors() - 1) {
                doublestep[i] = true;
            } else if (_currRotors.get(i + 1).atNotch()) {
                doublestep[i] = true;
                doublestep[i + 1] = true;
            }
        }
        for (int i = 0; i < numRotors(); i++) {
            if (doublestep[i]) {
                _currRotors.get(i).advance();
            }
        }
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        doublestepHelper();
        int encryptfor;
        if (_plugboard == null) {
            encryptfor = c;
        } else {
            encryptfor = _plugboard.permute(c);
        }

        int rotorpos = _currRotors.size() - 1;
        while (rotorpos >= 0) {
            Rotor current = _currRotors.get(rotorpos);
            encryptfor = current.convertForward(encryptfor);
            rotorpos--;
        }

        int encryptback = encryptfor;
        int rotorposback = 1;
        while (rotorposback < _currRotors.size()) {
            Rotor current = _currRotors.get(rotorposback);
            encryptback = current.convertBackward(encryptback);
            rotorposback++;
        }
        if (_plugboard == null) {
            return encryptback;
        } else {
            return _plugboard.invert(encryptback);
        }
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";
        char[] msgArray = msg.toCharArray();
        for (char input : msgArray) {
            result += _alphabet.toChar(convert(_alphabet.toInt(input)));
        }
        return result;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotors in this machine. */
    private int _numRotors;

    /** Number of pawls in this machine.  */
    private int _numPawls;

    /** the plugboard for this machine. */
    private Permutation _plugboard;

    /** All the current rotors being in the machine. */
    private Rotor[] _allRotors;

    /** An array list for easy access to rotors. */
    private ArrayList<Rotor> _currRotors;


}
