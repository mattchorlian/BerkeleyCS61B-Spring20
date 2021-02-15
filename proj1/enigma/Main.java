package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Matt Chorlian
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }


    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine enigma = readConfig();
        String settings = _input.nextLine();
        String encryption = "";
        setUp(enigma, settings);
        while (_input.hasNextLine()) {
            String next = _input.nextLine();
            if (next.equals("") && !_input.hasNext()) {
                _output.print("");
            }
            if (next.contains("*")) {
                setUp(enigma, next);
            } else {
                encryption = enigma.convert(next.replaceAll(" ", ""));
                printMessageLine(encryption);
            }
        }
    }

    /** Helper function for readConfig() which finds invalid inputs. */
    private void exceptioncatcher() {
        if (!_config.hasNextInt()) {
            throw new EnigmaException("Invalid configuration");
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String chars = _config.next();
            char[] temp = chars.toCharArray();
            for (char ch : temp) {
                if ((ch == '(' || ch == ')' || ch == '*')) {
                    throw new EnigmaException("Configuration invalid");
                }
            }
            _alphabet = new Alphabet(chars);
            exceptioncatcher();
            int numRotors = _config.nextInt();
            exceptioncatcher();
            int numPawls = _config.nextInt();
            if (numPawls >= numRotors) {
                throw new EnigmaException("Invalid Configuration");
            }

            while (_config.hasNext()) {
                rotor = _config.next();
                String type = _config.next();
                rotortype = Character.toUpperCase(type.charAt(0));
                notches = type.substring(1);
                rotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, numPawls, rotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String permutation = _config.nextLine();
            while (_config.hasNext("\\(.*\\)")) {
                permutation = permutation + _config.nextLine();
            }
            int right = 0;
            int left = 0;
            char[] temp = permutation.toCharArray();
            for (char ch : temp) {
                if (ch == '(') {
                    left += 1;
                } else if (ch == ')') {
                    right += 1;
                }
            }
            if (right != left) {
                throw new EnigmaException("Bad permutation form");
            }

            permutation = permutation.replaceAll("\\s", " ");
            _permutation = new Permutation(permutation, _alphabet);

            if (rotortype == 'R') {
                return new Reflector(rotor, _permutation);
            } else if (rotortype == 'N') {
                return new FixedRotor(rotor, _permutation);
            } else if (rotortype == 'M') {
                return new MovingRotor(rotor, _permutation, notches);
            } else {
                throw new EnigmaException("Invalid Rotor Type");
            }


        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** @param M machine from set up: helper for setting the plugbboard. */
    private void plugboardsetUp(Machine M) {
        String plugboard = "";
        while (machinesettings.hasNext("\\(.*\\)")) {
            plugboard += " " + machinesettings.next();
        }
        if (plugboard.length() > 0) {
            Permutation perm = new Permutation(plugboard, _alphabet);
            M.setPlugboard(perm);
        }
    }


    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] settingsArray = settings.split(" ");
        String[] rotorlist = new String[M.numRotors()];
        machinesettings = new Scanner(settings);
        try {
            int index = 0;
            while (index < M.numRotors()) {
                if (!machinesettings.hasNext("[(]\\w+[)]")) {
                    String temp = machinesettings.next();
                    if (temp.charAt(0) == '*' && temp.length() > 1) {
                        rotorlist[index] = temp.substring(1);
                        index += 1;
                    } else if (temp.charAt(0) != '*') {
                        rotorlist[index] = temp;
                        index += 1;
                    }
                }
            }
            String set = machinesettings.next();
            if (set.length() != M.numRotors() - 1) {
                throw new EnigmaException("Bad Setting");
            }
            char[] temp = set.toCharArray();
            for (char c : temp) {
                if (!_alphabet.contains(c)) {
                    throw new EnigmaException("Bad setting format");
                }
            }
            M.insertRotors(rotorlist);
            M.setRotors(set);
            plugboardsetUp(M);
        } catch (NoSuchElementException exception) {
            throw new EnigmaException("Bad format during machine set up");
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        String result = "";
        while (msg.length() >= 6) {
            result += msg.substring(0, 5) + " ";
            msg = msg.substring(5);
        }
        result += msg;
        _output.println(result);
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** Rotors inputted to the machine. */
    private ArrayList<Rotor> rotors = new ArrayList<Rotor>();

    /** rotor type. */
    private char rotortype;

    /** permutation for a rotor. **/
    private Permutation _permutation;

    /** current rotor being added to machine. */
    private String rotor;

    /** notches for a moving rotor. */
    private String notches;

    /** settings to be read for this machine. **/
    private Scanner machinesettings;

}
