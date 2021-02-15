package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Matt Chorlian
 */
class Alphabet {

    /** alphabet. */
    private char[] _alphabet;

    /** _chars for alphabet. */
    private String _chars;


    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {

        char[] alpha = new char[chars.length()];
        for (int i = 0; i < chars.length(); i++) {
            alpha[i] = chars.charAt(i);
        }
        this._alphabet = alpha;
        this._chars = chars;
    }


    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _chars.length();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        for (int i = 0; i < _chars.length(); i++) {
            if (_alphabet[i] == (ch)) {
                return true;
            }
        }
        return false;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        if (index >= 0 && index <= size()) {
            return _alphabet[index];
        } else {
            throw new EnigmaException("index out of bounds");
        }
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        if (_chars.contains(".") || _chars.contains("_")) {
            for (int index = 0; index < _chars.length(); index++) {
                if (ch == _alphabet[index]) {
                    return index;
                }
            }
        }
        ch = Character.toUpperCase(ch);
        for (int index = 0; index < _chars.length(); index++) {
            if (ch == _alphabet[index]) {
                return index;
            }
        }
        throw new EnigmaException("no such index");
    }

}
