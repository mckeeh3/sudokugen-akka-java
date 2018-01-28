package sudokugen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

interface CellUnassigned {
    class State implements Serializable {
        final int row;
        final int col;
        final List<Integer> possibleValues;

        State(int row, int col, List<Integer> possibleValues) {
            this.row = row;
            this.col = col;
            this.possibleValues = new ArrayList<>(possibleValues);
        }

        @Override
        public String toString() {
            return String.format("%s[(%d, %d) %s]", getClass().getSimpleName(), row, col, possibleValues);
        }
    }

    class Ack implements Serializable {
        final State state;

        Ack(int row, int col, List<Integer> possibleValues) {
            this.state = new State(row, col, possibleValues);
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(), state);
        }
    }
}
