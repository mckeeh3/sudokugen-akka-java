package sudokugen;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.List;

class BoardActor extends AbstractLoggingActor {
    private int requiredAck;
    private int cycles;
    private CellsUnassigned.Ack cellSelected;
    private ActorRef runner;

    private final ActorRef cellsAssigned;
    private final ActorRef cellsUnassigned;
    private final ActorRef rows;
    private final ActorRef cols;
    private final ActorRef boxes;

    {
        cellsAssigned = getContext().actorOf(CellsAssignedActor.props(), "cellsAssigned");
        cellsUnassigned = getContext().actorOf(CellsUnassignedActor.props(), "cellsUnassigned");
        rows = getContext().actorOf(RowsActor.props(), "rows");
        cols = getContext().actorOf(ColsActor.props(), "cols");
        boxes = getContext().actorOf(BoxesActor.props(), "boxes");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Board.Generate.class, this::generate)
                .match(Board.SetCell.class, this::setCell)
                .match(Rows.Ack.class, this::rowsAck)
                .match(Cols.Ack.class, this::colsAck)
                .match(Boxes.Ack.class, this::boxesAck)
                .match(CellsUnassigned.Ack.class, this::cellsUnassignedAck)
                .match(Board.Invalid.class, this::boardInvalid)
                .match(Board.Generated.class, this::boardGenerated)
                .build();
    }

    @SuppressWarnings("unused")
    private void generate(Board.Generate generate) {
        setCell(randomSetCell());
        runner = getSender();
    }

    private void setCell(Board.SetCell setCell) {
        requiredAck = 1; // 4; // expecting acks from rows, cols, boxes, and unassigned cells

//        log().debug("{} {}", requiredAck, setCell);

        cellsAssigned.tell(setCell, getSelf());
        cellsUnassigned.tell(setCell, getSelf());
//        rows.tell(setCell, getSelf());
//        cols.tell(setCell, getSelf());
//        boxes.tell(setCell, getSelf());
    }

    @SuppressWarnings("unused")
    private void rowsAck(Rows.Ack rowsAck) {
//        log().debug("requiredAck {} rows", requiredAck);
        checkIfAllRowsColsBoxesAndCellsResponded();
    }

    @SuppressWarnings("unused")
    private void colsAck(Cols.Ack colsAck) {
//        log().debug("requiredAck {} cols", requiredAck);
        checkIfAllRowsColsBoxesAndCellsResponded();
    }

    @SuppressWarnings("unused")
    private void boxesAck(Boxes.Ack boxesAck) {
//        log().debug("requiredAck {} boxes", requiredAck);
        checkIfAllRowsColsBoxesAndCellsResponded();
    }

    private void cellsUnassignedAck(CellsUnassigned.Ack cellsUnassignedAck) {
//        log().debug("requiredAck {} cells", requiredAck);
        cellSelected = cellsUnassignedAck;
        checkIfAllRowsColsBoxesAndCellsResponded();
    }

    private void boardInvalid(Board.Invalid boardInvalid) {
        log().info("{} {}", boardInvalid, getContext().getParent());
        runner.tell(boardInvalid, getSelf());
        getContext().stop(getSelf());
    }

    private void boardGenerated(Board.Generated boardGenerated) {
        runner.tell(boardGenerated, getSelf());
        getContext().stop(getSelf());
    }

    private Board.SetCell randomSetCell() {
        int row = Random.inRange(1, 9);
        int col = Random.inRange(1, 9);
        int value = Random.inRange(1, 9);
        Board.Cell cell = new Board.Cell(row, col, value);

        return new Board.SetCell(cell);
    }

    private void checkIfAllRowsColsBoxesAndCellsResponded() {
        requiredAck--;
        if (requiredAck == 0) {
            setCell(cellSelected.row, cellSelected.col, cellSelected.possibleValues);
        }
    }

    private void setCell(int row, int col, List<Integer> possibleValues) {
        log().debug("{}", String.format("Cycles %d (%d, %d) %s", ++cycles, row, col, possibleValues));
        setCell(new Board.SetCell(new Board.Cell(row, col, Random.inList(possibleValues))));
    }

    static Props props() {
        return Props.create(BoardActor.class);
    }
}
