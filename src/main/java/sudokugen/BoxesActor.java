package sudokugen;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;

class BoxesActor extends AbstractLoggingActor {
    private int requiredAck;
    private List<ActorRef> boxes = new ArrayList<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Board.SetCell.class, this::setCell)
                .match(Box.Ack.class, this::boxAck)
                .match(Box.AckStop.class, this::boxAckStop)
                .match(Box.AckSetCell.class, this::boxAckSetCell)
                .build();
    }

    @Override
    public void preStart() {
        for (int row = 1; row <= 3; row++) {
            for (int col = 1; col <= 3; col++) {
                for (int value = 1; value <= 9; value++) {
                    int box = (row - 1) * 3 + col;
                    String name = String.format("box-%d-value-%d", box, value);
                    boxes.add(getContext().actorOf(BoxActor.props(box, value), name));
                }
            }
        }
    }

    private void setCell(Board.SetCell setCell) {
        requiredAck += boxes.size();
        for (ActorRef box : boxes) {
            box.tell(setCell, getSelf());
        }
    }

    @SuppressWarnings("unused")
    private void boxAck(Box.Ack ack) {
        checkIfAllBoxesResponded();
    }

    @SuppressWarnings("unused")
    private void boxAckStop(Box.AckStop ackStop) {
        boxes.remove(getSender());
        checkIfAllBoxesResponded();
    }

    private void boxAckSetCell(Box.AckSetCell ackSetCell) {
        getContext().getParent().tell(ackSetCell.setCell, getSelf());
        boxes.remove(getSender());
        log().debug("{} {} {}", boxes.size(), requiredAck, ackSetCell);
        checkIfAllBoxesResponded();
    }

    private void checkIfAllBoxesResponded() {
        requiredAck--;
        if (requiredAck == 0) {
            getContext().getParent().tell(new Boxes.Ack(), getSelf());
        }
    }

    static Props props() {
        return Props.create(BoxesActor.class);
    }
}
