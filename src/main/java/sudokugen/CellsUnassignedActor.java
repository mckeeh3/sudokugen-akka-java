package sudokugen;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class CellsUnassignedActor extends AbstractLoggingActor {
    private int requiredAck;
    private CellUnassigned.State cellUnassignedState;
    private List<ActorRef> cells = new ArrayList<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Board.SetCell.class, this::setCell)
                .match(CellUnassigned.Ack.class, this::cellUnassignedAck)
                .build();
    }

    @Override
    public void preStart() {
        for (int row = 1; row <= 9; row++) {
            for (int col = 1; col <= 9; col++) {
                cells.add(getContext().actorOf(CellUnassignedActor.props(row, col), cellActorName(row, col)));
            }
        }
    }

    private void setCell(Board.SetCell setCell) {
        if (cells.isEmpty()) {
            getSender().tell(new Board.AllCellsAssigned(), getSelf());
        } else {
            requiredAck += cells.size();
            for (ActorRef cell : cells) {
                cell.tell(setCell, getSelf());
            }
        }
    }

    @SuppressWarnings("unused")
    private void cellUnassignedAck(CellUnassigned.Ack ack) {
        filter(ack.state);
        checkIfAllCellsResponded();
    }

    private String cellActorName(int row, int col) {
        return String.format("unassigned-%d-%d", row, col);
    }

    private void filter(CellUnassigned.State ackState) {
        if (ackState.possibleValues.isEmpty()) {
            cells.remove(getSender());
        } else {
            if (cellUnassignedState == null) {
                cellUnassignedState = ackState;
            } else if (ackState.possibleValues.size() <= cellUnassignedState.possibleValues.size()) {
                cellUnassignedState = ackState;
            }
        }
    }

    private void checkIfAllCellsResponded() {
        requiredAck--;
        if (requiredAck == 0 && cellUnassignedState != null) {
            getContext().getParent().tell(new CellsUnassigned.Ack(cellUnassignedState), getSelf());
            cellUnassignedState = null;
        }
    }

    static Props props() {
        return Props.create(CellsUnassignedActor.class);
    }
}
