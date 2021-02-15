package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Matt Chorlian
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
        _setting = 0;
    }

    @Override
    void advance() {
        this.set(permutation().wrap(this.setting() + 1));
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    boolean atNotch() {
        String val = String.valueOf(alphabet().toChar(setting()));
        return _notches.contains(val);
    }

    /** notches for a moving rotor. */
    private String _notches;

    /** setting for a moving rotor. */
    private int _setting;


}
