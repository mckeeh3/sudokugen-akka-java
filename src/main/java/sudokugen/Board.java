package sudokugen;

import java.io.Serializable;
import java.util.Objects;

interface Board {
    class SetCell implements Serializable {
        final Cell cell;

        SetCell(Cell cell) {
            this.cell = cell;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SetCell setCell = (SetCell) o;
            return Objects.equals(cell, setCell.cell);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cell);
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(), cell);
        }
    }

    class Generate implements Serializable {
        @Override
        public String toString() {
            return String.format("%s[]", getClass().getSimpleName());
        }
    }

    class Generated implements Serializable {
        final Grid grid;

        Generated(Grid grid) {
            this.grid = grid;
        }

        @Override
        public String toString() {
            return String.format("%s[]", getClass().getSimpleName());
        }
    }

    class AllCellsAssigned implements Serializable {
        @Override
        public String toString() {
            return String.format("%s[]", getClass().getSimpleName());
        }
    }

    class Invalid implements Serializable {
        private final String message;

        Invalid(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(), message);
        }
    }

    class Cell implements Serializable {
        final int row;
        final int col;
        final int value;

        Cell(int row, int col, int value) {
            this.row = row;
            this.col = col;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cell cell = (Cell) o;
            return row == cell.row &&
                    col == cell.col &&
                    value == cell.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col, value);
        }

        @Override
        public String toString() {
            return String.format("%s[(%d, %d) = %d]", getClass().getSimpleName(), row, col, value);
        }
    }

    class Grid implements Serializable {
        private Row[] grid = new Row[9];

        {
            for (int row = 0; row < 9; row++) {
                grid[row] = new Row();
            }
        }

        private static class Row {
            private final Cell[] row = new Cell[9];
        }

        void set(Cell cell) {
            grid[cell.row - 1].row[cell.col - 1] = cell;
        }

        Cell get(int row, int col) {
            return grid[row - 1].row[col - 1];
        }

        @Override
        public String toString() {
            String delimiter = "| ";
            StringBuilder grid = new StringBuilder();
            for (int row = 1; row <= 9; row++) {
                grid.append("-------------------------------------\n");
                for (int col = 1; col <= 9; col++) {
                    Cell cell = get(row, col);
                    int value = cell == null ? 0 : cell.value;
                    grid.append(delimiter).append(value == 0 ? " " : value);
                    delimiter = " | ";
                }
                grid.append(" |\n");
                delimiter = "| ";
            }
            grid.append("-------------------------------------\n");

            return grid.toString();
        }
    }
}
