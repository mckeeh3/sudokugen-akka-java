package sudokugen;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class ColsActor extends AbstractLoggingActor {
    private int requiredAck;
    private List<ActorRef> cols = new ArrayList<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Board.SetCell.class, this::setCell)
                .match(Col.Ack.class, this::colAck)
                .match(Col.AckStop.class, this::colAckStop)
                .match(Col.AckSetCell.class, this::colAckSetCell)
                .build();
    }

    @Override
    public void preStart() {
        for (int col = 1; col <= 9; col++) {
            for (int value = 1; value <= 9; value++) {
                String name = String.format("col-%d-value-%d", col, value);
                cols.add(getContext().actorOf(ColActor.props(col, value), name));
            }
        }
    }

    private void setCell(Board.SetCell setCell) {
        requiredAck += cols.size();
        for (ActorRef col : cols) {
            col.tell(setCell, getSelf());
        }
    }

    @SuppressWarnings("unused")
    private void colAck(Col.Ack ack) {
        checkIfAllColsResponded();
    }

    @SuppressWarnings("unused")
    private void colAckStop(Col.AckStop ackStop) {
        cols.remove(getSender());
        checkIfAllColsResponded();
    }

    private void colAckSetCell(Col.AckSetCell ackSetCell) {
        getContext().getParent().tell(ackSetCell.setCell, getSelf());
        cols.remove(getSender());
        log().debug("{} {} {}", cols.size(), requiredAck, ackSetCell);
        checkIfAllColsResponded();
    }

    private void checkIfAllColsResponded() {
        requiredAck--;
        if (requiredAck == 0) {
            getContext().getParent().tell(new Cols.Ack(), getSelf());
        }
    }

    static Props props() {
        return Props.create(ColsActor.class);
    }
}
