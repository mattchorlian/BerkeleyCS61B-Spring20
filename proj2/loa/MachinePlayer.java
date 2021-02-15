/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;


import java.util.ArrayList;
import java.util.List;


import static loa.Piece.*;

/** An automated Player.
 *  @author Matt Chorlian
 */
class MachinePlayer extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new MachinePlayer with no piece or controller (intended to produce
     *  a template). */
    MachinePlayer() {
        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert side() == work.turn();
        _foundMove = null;
        if (side() == WP) {
            value = findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            value = findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove.*/
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {

        if (depth == 0 || board.gameOver()) {
            return heuristic(board);
        }
        Move bestmove = null;
        int heuristic = 0;

        if (sense == 1) {
            List allmoves = board.legalMoves();
            for (Object move : allmoves) {
                board.makeMove((Move) move);
                heuristic = findMove(board, depth - 1, saveMove,
                        sense * -1, alpha, beta);
                if (heuristic > alpha && saveMove) {
                    bestmove = (Move) move;
                    alpha = heuristic;
                }
                if (beta <= alpha) {
                    board.retract();
                    break;
                }
                board.retract();
            }
        }

        if (sense == -1) {
            List allmoves = board.legalMoves();
            for (Object move : allmoves) {
                board.makeMove((Move) move);
                heuristic = findMove(board, depth - 1, saveMove,
                        sense * -1, alpha, beta);
                if (heuristic < beta) {
                    bestmove = (Move) move;
                    beta = heuristic;
                }
                if (beta <= alpha) {
                    board.retract();
                    break;
                }
                board.retract();
            }
        }


        if (saveMove) {
            _foundMove = bestmove;
        }

        return heuristic;
    }

    /** method to determine the value of a state of the board.
    * @param board .
     * @return int*/
    private int heuristic(Board board) {
        int innersquareweight = 5;
        int numclustersweight = getBoard().movesMade() / 5;
        int valuation = 0;

        if (board.piecesContiguous(WP)) {
            return WINNING_VALUE;
        } else if (board.piecesContiguous(BP)) {
            return -WINNING_VALUE;
        }
        int totalblack = 0;
        int totalwhite = 0;
        for (int clust : board.getRegionSizes(BP)) {
            totalblack += clust;
        }
        for (int clust : board.getRegionSizes(WP)) {
            totalwhite += clust;
        }
        valuation += (totalblack - totalwhite);
        List<Square> whites = new ArrayList<>();
        List<Square> blacks = new ArrayList<>();
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                Square curr = Square.sq(col, row);
                if (board.get(curr).compareTo(WP) == 0) {
                    whites.add(curr);
                } else if (board.get(curr).compareTo(BP) == 0) {
                    blacks.add(curr);
                }
            }
        }
        int totalwhitespread = 0;
        int totalblackspread = 0;
        for (int i = 0; i < whites.size(); i++) {
            for (int j = i + 1; j < whites.size(); j++) {
                totalwhitespread += whites.get(i).distance(whites.get(j));
            }
        }
        for (int i = 0; i < blacks.size(); i++) {
            for (int j = i + 1; j < blacks.size(); j++) {
                totalblackspread += blacks.get(i).distance(blacks.get(j));
            }
        }
        int avgwhitespread = Math.floorDiv(totalwhitespread, whites.size() * 3);
        int avgblackspread = Math.floorDiv(totalblackspread, blacks.size() * 3);
        valuation += (avgblackspread - avgwhitespread);
        int blackclusters = board.getRegionSizes(BP).size();
        int whiteclusters = board.getRegionSizes(WP).size();
        int clusterdiff = blackclusters - whiteclusters;
        valuation += clusterdiff * numclustersweight;

        valuation += heuristic2(board) * innersquareweight;
        return valuation;
    }

    /** heuristic 2 since first one too long.
     * @param board  .
     * @return int*/
    private int heuristic2(Board board) {
        int numblack = 0;
        int numwhite = 0;
        List<Square> centrals = new ArrayList<>();
        Square c3 = Square.sq("c3");
        centrals.add(c3);
        Square c4 = Square.sq("c4");
        centrals.add(c4);
        Square c5 = Square.sq("c5");
        centrals.add(c5);
        Square c6 = Square.sq("c6");
        centrals.add(c6);
        Square d3 = Square.sq("d3");
        centrals.add(d3);
        Square d4 = Square.sq("d4");
        centrals.add(d4);
        Square d5 = Square.sq("d5");
        centrals.add(d5);
        Square d6 = Square.sq("d6");
        centrals.add(d6);
        Square e3 = Square.sq("e3");
        centrals.add(e3);
        Square e4 = Square.sq("e4");
        centrals.add(e4);
        Square e5 = Square.sq("e5");
        centrals.add(e5);
        Square e6 = Square.sq("e6");
        centrals.add(e6);
        Square f3 = Square.sq("f3");
        centrals.add(f3);
        Square f4 = Square.sq("f4");
        centrals.add(f4);
        Square f5 = Square.sq("f5");
        centrals.add(f5);
        Square f6 = Square.sq("f6");
        centrals.add(f6);
        for (Square sq : centrals) {
            if (board.get(sq).compareTo(BP) == 0) {
                numblack++;
            } else if (board.get(sq).compareTo(WP) == 0) {
                numwhite++;
            }
        }
        int total = numwhite - numblack;
        return total;
    }


    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return 3;
    }


    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;

}
