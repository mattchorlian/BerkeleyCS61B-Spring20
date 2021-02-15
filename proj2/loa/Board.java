/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Square.*;

/** Represents the state of a game of Lines of Action.
 *  @author Matt Chorlian
 */
class Board {

    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 60;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row][col]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. */
    void initialize(Piece[][] contents, Piece side) {
        int index = 0;
        while (index < 64) {
            for (int i = 0; i < contents.length; i++) {
                for (int j = 0; j < contents[0].length; j++) {
                    _board[index] = contents[i][j];
                    index++;
                }
            }
        }
        _turn = side;
        _moveLimit = DEFAULT_MOVE_LIMIT;
        _winnerKnown = false;
        _moves.clear();
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        this._turn = board._turn;
        for (int i = 0; i < 64; i++) {
            _board[i] = board._board[i];
        }
    }

    /** Return the contents of the square at SQ. */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /** Set the square at SQ to V and set the side that is to move next
     *  to NEXT, if NEXT is not null. */
    void set(Square sq, Piece v, Piece next) {
        int index = sq.index();
        _board[index] = v;
        if (next != null) {
            _turn = next;
        }
    }

    /** Set the square at SQ to V, without modifying the side that
     *  moves next. */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }

    /** Set limit on number of moves by each side that results in a tie to
     *  LIMIT, where 2 * LIMIT > movesMade(). */
    void setMoveLimit(int limit) {
        if (2 * limit <= movesMade()) {
            throw new IllegalArgumentException("move limit too small");
        }
        _moveLimit = 2 * limit;
    }

    /** Assuming isLegal(MOVE), make MOVE. This function assumes that
     *  MOVE.isCapture() will return false.  If it saves the move for
     *  later retraction, makeMove itself uses MOVE.captureMove() to produce
     *  the capturing move. */
    void makeMove(Move move) {
        assert isLegal(move);
        Square start = move.getFrom();
        Square end = move.getTo();
        if (_board[end.index()] == _turn.opposite()) {
            _moves.add(Move.mv(start, end, true));
        } else {
            _moves.add(move);
        }
        _board[start.index()] = EMP;
        set(end, _turn, _turn.opposite());
        this._subsetsInitialized = false;
    }

    /** Retract (unmake) one move, returning to the state immediately before
     *  that move.  Requires that movesMade () > 0. */
    void retract() {
        assert movesMade() > 0;
        Move lastmove = _moves.get(_moves.size() - 1);
        _moves.remove(_moves.size() - 1);
        if (lastmove.isCapture()) {
            Square captured = lastmove.getTo();
            set(captured, _turn);
            Square returnback = lastmove.getFrom();
            set(returnback, _turn.opposite());
        } else {
            Square returnback = lastmove.getFrom();
            Square emptied = lastmove.getTo();
            set(returnback, _turn.opposite());
            _board[emptied.index()] = EMP;
        }
        _turn = _turn.opposite();
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff FROM - TO is a legal move for the player currently on
     *  move. */
    boolean isLegal(Square from, Square to) {
        boolean blocked = !blocked(from, to);

        if (_board[from.index()] == _turn) {
            int count = 1;
            int steps = 1;
            int direct = from.direction(to);
            int oppdirect = to.direction(from);
            while (from.moveDest(direct, steps) != null) {
                Square curr = from.moveDest(direct, steps);
                if (_board[curr.index()] != EMP) {
                    count++;
                }
                steps++;
            }
            steps = 1;
            while (from.moveDest(oppdirect, steps) != null) {
                Square curr = from.moveDest(oppdirect, steps);
                if (_board[curr.index()] != EMP) {
                    count++;
                }
                steps++;
            }
            return (blocked && (count == from.distance(to)));
        }
        return false;
    }

    /** Return true iff MOVE is legal for the player currently on move.
     *  The isCapture() property is ignored. */
    boolean isLegal(Move move) {
        return isLegal(move.getFrom(), move.getTo());
    }

    /** @return Method which determines legal moves for a given turn.*/
    List<Move> legalMoves() {
        ArrayList<Move> allmoves1 = new ArrayList<>();
        ArrayList<Square> validsq = new ArrayList<>();
        ArrayList<Square> validlandings = new ArrayList<>();
        for (Square sq : ALL_SQUARES) {
            if (_board[sq.index()] == _turn) {
                validsq.add(sq);
            } else if (_board[sq.index()] != _turn) {
                validlandings.add(sq);
            }
        }
        for (Square sq : validsq) {
            for (Square landing : validlandings) {
                if (sq.isValidMove(landing) && isLegal(sq, landing)) {
                    allmoves1.add(Move.mv(sq, landing));
                }
            }
        }
        return allmoves1;
    }


    /** Return true iff the game is over (either player has all his
     *  pieces continguous or there is a tie). */
    boolean gameOver() {
        return winner() != null;
    }

    /** Return true iff SIDE's pieces are contiguous. */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }

    /** Return the winning side, if any.  If the game is not over, result is
     *  null.  If the game has ended in a tie, returns EMP. */
    Piece winner() {
        _subsetsInitialized = false;
        if (!_winnerKnown) {
            if (piecesContiguous(WP) && piecesContiguous(BP)) {
                _winner = _turn.opposite();
                _winnerKnown = true;
            } else if (piecesContiguous(WP)) {
                _winner = WP;
                _winnerKnown = true;
            } else if (piecesContiguous(BP)) {
                _winner = BP;
                _winnerKnown = true;
            } else if (movesMade() >= _moveLimit) {
                _winner = EMP;
                _winnerKnown = true;
            } else {
                return null;
            }

        }
        return _winner;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===\n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("\n");
        }
        out.format("Next move: %s\n===", turn().fullName());
        return out.toString();
    }

    /** Return true if a move from FROM to TO is blocked by an opposing
     *  piece or by a friendly piece on the target square. */
    private boolean blocked(Square from, Square to) {
        int direct = from.direction(to);
        Piece end = _board[to.index()];
        Piece start = _board[from.index()];
        if (end == start) {
            return true;
        }
        int step = 1;
        while (from.moveDest(direct, step) != null
                && from.moveDest(direct, step) != to) {
            Square current = from.moveDest(direct, step);
            if (_board[current.index()] == _turn.opposite()) {
                return true;
            }
            step++;
        }
        return false;
    }

    /** Return the size of the as-yet unvisited cluster of squares
     *  containing P at and adjacent to SQ.  VISITED indicates squares that
     *  have already been processed or are in different clusters.  Update
     *  VISITED to reflect squares counted. */
    private int numContig(Square sq, boolean[][] visited, Piece p) {
        int result = 1;
        Square[] adjacents = sq.adjacent();
        visited[sq.col()][sq.row()] = true;
        for (Square square : adjacents) {
            if (!visited[square.col()][square.row()]
                    && _board[square.index()] == p) {
                visited[square.col()][square.row()] = true;
                result += numContig(square, visited, p);
            }
        }
        return result;
    }

    /** Set the values of _whiteRegionSizes and _blackRegionSizes. */
    private void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }

        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();
        ArrayList<Square> blacksquares = new ArrayList<>();
        ArrayList<Square> whitesquares = new ArrayList<>();
        boolean[][] visits = new boolean[8][8];

        for (Square sq : ALL_SQUARES) {
            if (_board[sq.index()] == BP) {
                blacksquares.add(sq);
            } else if (_board[sq.index()] == WP) {
                whitesquares.add(sq);
            }
        }

        for (Square sq : blacksquares) {
            if (!visits[sq.col()][sq.row()]) {
                _blackRegionSizes.add(numContig(sq, visits, BP));
            }
        }

        for (Square sq : whitesquares) {
            if (!visits[sq.col()][sq.row()]) {
                _whiteRegionSizes.add(numContig(sq, visits, WP));
            }
        }

        Collections.sort(_whiteRegionSizes, Collections.reverseOrder());
        Collections.sort(_blackRegionSizes, Collections.reverseOrder());
        _subsetsInitialized = true;
    }

    /** Return the sizes of all the regions in the current union-find
     *  structure for side S. */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {
            return _whiteRegionSizes;
        } else {
            return _blackRegionSizes;
        }
    }


    /** The standard initial configuration for Lines of Action (bottom row
     *  first). */
    static final Piece[][] INITIAL_PIECES = {
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };

    /** Current contents of the board.  Square S is at _board[S.index()]. */
    private final Piece[] _board = new Piece[BOARD_SIZE  * BOARD_SIZE];

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;
    /** Limit on number of moves before tie is declared.  */
    private int _moveLimit;
    /** True iff the value of _winner is known to be valid. */
    private boolean _winnerKnown;
    /** Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     *  in progress).  Use only if _winnerKnown. */
    private Piece _winner;

    /** True iff subsets computation is up-to-date. */
    private boolean _subsetsInitialized;

    /** List of the sizes of continguous clusters of pieces, by color. */
    private final ArrayList<Integer>
        _whiteRegionSizes = new ArrayList<>(),
        _blackRegionSizes = new ArrayList<>();
    /** a list of all legal moves for this turn. **/
    private ArrayList allmoves;
}
