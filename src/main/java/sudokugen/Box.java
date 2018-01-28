package sudokugen;

import java.io.Serializable;

interface Box {
    class Ack implements Serializable {
        final int box;

        Ack(int box) {
            this.box = box;
        }

        @Override
        public String toString() {
            return String.format("%s[%d]", getClass().getSimpleName(), box);
        }
    }

    class AckStop implements Serializable {
        final int box;

        AckStop(int box) {
            this.box = box;
        }

        @Override
        public String toString() {
            return String.format("%s[(%d)]", getClass().getSimpleName(), box);
        }
    }

    class AckSetCell implements Serializable {
        final int box;
        final Board.SetCell setCell;

        AckSetCell(int box, Board.SetCell setCell) {
            this.box = box;
            this.setCell = setCell;
        }

        @Override
        public String toString() {
            return String.format("%s[(%d) %s]", getClass().getSimpleName(), box, setCell);
        }
    }
}
