package sudokugen;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class ColActor extends AbstractLoggingActor {
    private final int col;
    private final int monitoredValue;
    private List<Board.Cell> monitoredCells = new ArrayList<>();

    private ColActor(int col, int monitoredValue) {
        this.col = col;
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
        for (int row = 1; row <= 9; row++) {
            monitoredCells.add(new Board.Cell(row, col, monitoredValue));
        }
    }

    private void setCell(Board.SetCell setCell) {
        removeInRow(setCell);
        removeInCol(setCell);
        removeInBox(setCell);
        removeInColAnyValue(setCell);

        if (monitoredCells.isEmpty()) {
            getContext().getParent().tell(new Col.AckStop(col), getSelf());
            monitoringComplete();
        } else if (monitoredCells.size() == 1) {
            Board.Cell cell = monitoredCells.get(0);
            Board.SetCell setCellAck = new Board.SetCell(new Board.Cell(cell.row, cell.col, monitoredValue));
            getContext().getParent().tell(new Col.AckSetCell(col, setCellAck), getSelf());
            monitoringComplete();
        } else {
            getContext().getParent().tell(new Col.Ack(col), getSelf());
        }
    }

    private boolean isInRow(Board.SetCell setCell, Board.Cell cell) {
        return setCell.cell.row == cell.row;
    }

    private boolean isInCol(Board.SetCell setCell) {
        return setCell.cell.col == col;
    }

    private boolean isMonitoredValue(Board.SetCell setCell) {
        return setCell.cell.value == monitoredValue;
    }

    private void monitoringComplete() {
        monitoredCells.clear();
    }

    private void removeInRow(Board.SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            monitoredCells.removeIf(cell -> isInRow(setCell, cell));
        }
    }

    private void removeInCol(Board.SetCell setCell) {
        if (isInCol(setCell) && isMonitoredValue(setCell)) {
            monitoredCells = new ArrayList<>();
        }
    }

    private void removeInBox(Board.SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            int setCellBox = boxFor(setCell);
            monitoredCells.removeIf(cell -> setCellBox == boxFor(cell));
        }
    }

    private void removeInColAnyValue(Board.SetCell setCell) {
        if (isInCol(setCell)) {
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

    static Props props(int col, int monitoredValue) {
        return Props.create(ColActor.class, col, monitoredValue);
    }
}
