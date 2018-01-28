package sudokugen;

import java.io.Serializable;

interface Boxes {
    class Ack implements Serializable {
        @Override
        public String toString() {
            return String.format("%s[]", getClass().getSimpleName());
        }
    }
}
