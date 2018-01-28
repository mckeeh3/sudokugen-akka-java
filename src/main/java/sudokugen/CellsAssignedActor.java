package sudokugen;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class CellsAssignedActor extends AbstractLoggingActor {
    private ActorRef validateBoard;
    private final List<String> assignedCells = new ArrayList<>();

    {
        validateBoard = getContext().actorOf(ValidateBoardActor.props(), "validate-board");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Board.SetCell.class, this::setCell)
                .match(Validate.Invalid.class, this::boardInvalid)
                .match(Validate.ValidBoard.class, this::boardValid)
                .build();
    }

    private void setCell(Board.SetCell setCell) {
        String rowCol = String.format("%d-%d", setCell.cell.row, setCell.cell.col);
        if (!assignedCells.contains(rowCol)) {
            assignedCells.add(rowCol);
            assignCell(setCell, cellActorName(setCell));
            validateBoard.tell(setCell, getSelf());
        }
    }

    private void boardInvalid(Validate.Invalid cellInvalid) {
        getContext().getParent().tell(new Board.Invalid(cellInvalid.toString()), getSelf());
    }

    private void boardValid(Validate.ValidBoard boardValid) {
        getContext().getParent().tell(new Board.Generated(boardValid.grid), getSelf());
    }

    private void assignCell(Board.SetCell setCell, String cellActorName) {
        log().debug("Assign {}", setCell);
        getContext().actorOf(CellAssignedActor.props(setCell.cell), cellActorName);
    }

    private String cellActorName(Board.SetCell setCell) {
        return String.format("assigned-%d-%d", setCell.cell.row, setCell.cell.col);
    }

    static Props props() {
        return Props.create(CellsAssignedActor.class);
    }
}
