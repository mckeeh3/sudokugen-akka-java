package sudokugen;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CellUnassignedActor extends AbstractLoggingActor {
    private final int row;
    private final int col;
    private final List<Integer> possibleValues;
    private final int boxIndex;

    private CellUnassignedActor(int row, int col) {
        this.row = row;
        this.col = col;
        boxIndex = boxFor(row, col);

        possibleValues = new ArrayList<>();
        Collections.addAll(possibleValues, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Board.SetCell.class, this::setCell)
                .build();
    }

    private void setCell(Board.SetCell setCell) {
        if (possibleValues.isEmpty()) {
            getContext().getParent().tell(new CellUnassigned.Ack(row, col, possibleValues), getSelf());
        } else if (isSameCell(setCell)) {
            cellSetBySameCell();
        } else if (isSameRowColOrBox(setCell)) {
            trimPossibleValues(setCell);
            getContext().getParent().tell(new CellUnassigned.Ack(row, col, possibleValues), getSelf());
        } else {
            getContext().getParent().tell(new CellUnassigned.Ack(row, col, possibleValues), getSelf());
        }
    }

    private boolean isSameCell(Board.SetCell setCell) {
        return row == setCell.cell.row && col == setCell.cell.col;
    }

    private boolean isSameRowColOrBox(Board.SetCell setCell) {
        return isSameRow(setCell) || isSameCol(setCell) || isSameBox(setCell);
    }

    private boolean isSameRow(Board.SetCell setCell) {
        return row == setCell.cell.row;
    }

    private boolean isSameCol(Board.SetCell setCell) {
        return col == setCell.cell.col;
    }

    private boolean isSameBox(Board.SetCell setCell) {
        return boxIndex == boxFor(setCell.cell.row, setCell.cell.col);
    }

    private void cellSetBySameCell() {
        possibleValues.clear();
        getContext().getParent().tell(new CellUnassigned.Ack(row, col, possibleValues), getSelf());
    }

    private void trimPossibleValues(Board.SetCell setCell) {
        possibleValues.removeIf(value -> value == setCell.cell.value);
    }

    private int boxFor(int row, int col) {
        int boxRow = (row - 1) / 3 + 1;
        int boxCol = (col - 1) / 3 + 1;
        return (boxRow - 1) * 3 + boxCol;
    }

    static Props props(int row, int col) {
        return Props.create(CellUnassignedActor.class, row, col);
    }
}
