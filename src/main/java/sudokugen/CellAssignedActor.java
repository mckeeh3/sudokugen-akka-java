package sudokugen;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

class CellAssignedActor extends AbstractLoggingActor {
    private final Board.Cell cell;

    private CellAssignedActor(Board.Cell cell) {
        this.cell = cell;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .build();
    }

    static Props props(Board.Cell cell) {
        return Props.create(CellAssignedActor.class, cell);
    }
}
