package sudokugen;

import java.io.Serializable;

interface Row {
    class Ack implements Serializable {
        final int row;
        final int value;

        Ack(int row, int value) {
            this.row = row;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%s[(%d) %d]", getClass().getSimpleName(), row, value);
        }
    }

    class AckStop implements Serializable {
        final int row;
        final int value;

        AckStop(int row, int value) {
            this.row = row;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%s[(%d) %d]", getClass().getSimpleName(), row, value);
        }
    }

    class AckSetCell implements Serializable {
        final int row;
        final int value;
        final Board.SetCell setCell;

        AckSetCell(int row, int value, Board.SetCell setCell) {
            this.row = row;
            this.value = value;
            this.setCell = setCell;
        }

        @Override
        public String toString() {
            return String.format("%s[(%d) %d, %s]", getClass().getSimpleName(), row, value, setCell);
        }
    }
}
