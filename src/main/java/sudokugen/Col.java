package sudokugen;

import java.io.Serializable;

interface Col {
    class Ack implements Serializable {
        final int col;

        Ack(int col) {
            this.col = col;
        }

        @Override
        public String toString() {
            return String.format("%s[(%d)]", getClass().getSimpleName(), col);
        }
    }

    class AckStop implements Serializable {
        final int col;

        AckStop(int col) {
            this.col = col;
        }

        @Override
        public String toString() {
            return String.format("%s[(%d)]", getClass().getSimpleName(), col);
        }
    }

    class AckSetCell implements Serializable {
        final int col;
        final Board.SetCell setCell;

        AckSetCell(int col, Board.SetCell setCell) {
            this.col = col;
            this.setCell = setCell;
        }

        @Override
        public String toString() {
            return String.format("%s[(%d) %s]", getClass().getSimpleName(), col, setCell);
        }
    }
}
