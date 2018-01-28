package sudokugen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

interface CellsUnassigned {
    class Ack implements Serializable {
        final int row;
        final int col;
        final List<Integer> possibleValues;

        Ack(int row, int col, List<Integer> possibleValues) {
            this.row = row;
            this.col = col;
            this.possibleValues = new ArrayList<>(possibleValues);
        }

        Ack(CellUnassigned.State state) {
            this(state.row, state.col, state.possibleValues);
        }

        @Override
        public String toString() {
            return String.format("%s[(%d, %d) %s]", getClass().getSimpleName(), row, col, possibleValues);
        }
    }
}
