package sudokugen;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

public class BoxActor extends AbstractLoggingActor {
    private final int box;
    private final int monitoredValue;
    private List<Board.Cell> monitoredCells = new ArrayList<>();

    private BoxActor(int box, int monitoredValue) {
        this.box = box;
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
        for (int r = 1; r <= 9; r++) {
            for (int c = 1; c <= 9; c++) {
                if (box == boxFor(r, c)) {
                    monitoredCells.add(new Board.Cell(r, c, monitoredValue));
                }
            }
        }
    }

    private void setCell(Board.SetCell setCell) {
        removeCellFromBoxSameValue(setCell);
        removeCellFromBoxAnyValue(setCell);
        removeCellFromRow(setCell);
        removeCellFromCol(setCell);

        if (monitoredCells.isEmpty()) {
            getContext().getParent().tell(new Box.AckStop(box), getSelf());
            monitoringComplete();
        } else if (monitoredCells.size() == 1) {
            Board.Cell cell = monitoredCells.get(0);
            Board.SetCell setCellAck = new Board.SetCell(new Board.Cell(cell.row, cell.col, monitoredValue));
            getContext().getParent().tell(new Box.AckSetCell(box, setCellAck), getSelf());
            monitoringComplete();
        } else {
            getContext().getParent().tell(new Box.Ack(box), getSelf());
        }
    }

    private int boxFor(int row, int col) {
        int boxRow = (row - 1) / 3 + 1;
        int boxCol = (col - 1) / 3 + 1;
        return (boxRow - 1) * 3 + boxCol;
    }

    private boolean isInBox(Board.SetCell setCell) {
        return box == boxFor(setCell.cell.row, setCell.cell.col);
    }

    private boolean isSameCell(Board.SetCell setCell, Board.Cell cell) {
        return cell.row == setCell.cell.row && cell.col == setCell.cell.col;
    }

    private boolean isMonitoredValue(Board.SetCell setCell) {
        return monitoredValue == setCell.cell.value;
    }

    private void monitoringComplete() {
        monitoredCells.clear();
    }

    private void removeCellFromBoxSameValue(Board.SetCell setCell) {
        if (isInBox(setCell) && isMonitoredValue(setCell)) {
            monitoredCells = new ArrayList<>();
        }
    }

    private void removeCellFromBoxAnyValue(Board.SetCell setCell) {
        monitoredCells.removeIf(cell -> isSameCell(setCell, cell));
    }

    private void removeCellFromRow(Board.SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            monitoredCells.removeIf(cell -> cell.row == setCell.cell.row);
        }
    }

    private void removeCellFromCol(Board.SetCell setCell) {
        if (isMonitoredValue(setCell)) {
            monitoredCells.removeIf(cell -> cell.col == setCell.cell.col);
        }
    }

    static Props props(int box, int monitoredValue) {
        return Props.create(BoxActor.class, box, monitoredValue);
    }
}
