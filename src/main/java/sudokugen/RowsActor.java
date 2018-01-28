package sudokugen;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class RowsActor extends AbstractLoggingActor {
    private int requiredAck;
    private List<ActorRef> rows = new ArrayList<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Board.SetCell.class, this::setCell)
                .match(Row.Ack.class, this::rowAck)
                .match(Row.AckStop.class, this::rowAckStop)
                .match(Row.AckSetCell.class, this::rowAckSetCell)
                .build();
    }

    @Override
    public void preStart() {
        for (int row = 1; row <= 9; row++) {
            for (int value = 1; value <= 9; value++) {
                String name = String.format("row-%d-value-%d", row, value);
                rows.add(getContext().actorOf(RowActor.props(row, value), name));
            }
        }
    }

    private void setCell(Board.SetCell setCell) {
        requiredAck += rows.size();
        for (ActorRef row : rows) {
            row.tell(setCell, getSelf());
        }
    }

    @SuppressWarnings("unused")
    private void rowAck(Row.Ack ack) {
        checkIfAllRowsResponded();
    }

    @SuppressWarnings("unused")
    private void rowAckStop(Row.AckStop ackStop) {
        rows.remove(getSender());
        checkIfAllRowsResponded();
    }

    private void rowAckSetCell(Row.AckSetCell ackSetCell) {
        rows.remove(getSender());
        checkIfAllRowsResponded();
        getContext().getParent().tell(ackSetCell.setCell, getSelf());
    }

    private void checkIfAllRowsResponded() {
        requiredAck--;
        if (requiredAck == 0) {
            getContext().getParent().tell(new Rows.Ack(), getSelf());
        }
    }

    static Props props() {
        return Props.create(RowsActor.class);
    }
}
