package sudokugen;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class RowActor extends AbstractLoggingActor {
    private final int row;
    private final int monitoredValue;
    private List<Board.Cell> monitoredCells = new ArrayList<>();

    private RowActor(int row, int monitoredValue) {
        this.row = row;
        this.monitoredValue = monitoredValue;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Board.SetCell.class, this::setCell)
                .build();
    }

    @Override
    public void preStart() {
        for (int col = 1; col <= 9; col++) {
            monitoredCells.add(new Board.Cell(row, col, monitoredValue));
        }
    }

    private void setCell(Board.SetCell setCell) {
        removeInRow(setCell);
        removeInCol(setCell);
        removeInBox(setCell);
        removeInRowAnyValue(setCell);

        if (monitoredCells.isEmpty()) {
            getContext().getParent().tell(new Row.AckStop(row, monitoredValue), getSelf());
            monitoringComplete();
        } else if (monitoredCells.size() == 1) {
            Board.Cell cell = monitoredCells.get(0);
            Board.SetCell setCellAck = new Board.SetCell(new Board.Cell(row, cell.col, monitoredValue));
            getContext().getParent().tell(new Row.AckSetCell(row, monitoredValue, setCellAck), getSelf());
            monitoringComplete();
        } else {
            getContext().getParent().tell(new Row.Ack(row, monitoredValue), getSelf());
        }
    }

    private boolean isInRow(Board.SetCell setCell) {
        return setCell.cell.row == row;
    }

    private boolean isInCol(Board.SetCell setCell, Board.Cell cell) {
        return setCell.cell.col == cell.col;
    }

    private boolean isMonitoredValue(Board.SetCell setCell) {
        return setCell.cell.value == monitoredValue;
    }

    private void monitoringComplete() {
        monitoredCells.clear();
    }

    private void removeInRow(Board.SetCell setCell) {
        if (isInRow(setCell) && isMonitoredValue(setCell)) {
            monitoredCells = new ArrayList<>();
        }
    }

    private void removeInCol(Board.SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            monitoredCells.removeIf(cell -> isInCol(setCell, cell));
        }
    }

    private void removeInBox(Board.SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            int setCellBox = boxFor(setCell);
            monitoredCells.removeIf(cell -> setCellBox == boxFor(cell));
        }
    }

    private void removeInRowAnyValue(Board.SetCell setCell) {
        if (isInRow(setCell)) {
            monitoredCells.removeIf(cell -> cell.row == setCell.cell.row && cell.col == setCell.cell.col);
        }
    }

    private int boxFor(Board.SetCell setCell) {
        return boxFor(setCell.cell);
    }

    private int boxFor(Board.Cell cell) {
        return boxFor(cell.row, cell.col);
    }

    private int boxFor(int row, int col) {
        int boxRow = (row - 1) / 3 + 1;
        int boxCol = (col - 1) / 3 + 1;

        return (boxRow - 1) * 3 + boxCol;
    }

    static Props props(int row, int monitoredValue) {
        return Props.create(RowActor.class, row, monitoredValue);
    }
}
