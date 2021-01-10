package org.faboo.example.routing.expander;

import org.neo4j.graphdb.Node;
import org.neo4j.values.storable.DurationValue;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class TraversalState {

    private final LocalDateTime startTime;
    private final DayOfWeek startDay;
    private final Node destination;
    private DurationValue lastArrivalTime;

    public TraversalState(LocalDateTime startTime,  Node destination) {
        this.startTime = startTime;
        this.startDay = startTime.getDayOfWeek();
        this.destination = destination;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public DayOfWeek getStartDay() {
        return startDay;
    }

    public Node getDestination() {
        return destination;
    }

    public DurationValue getLastArrivalTime() {
        return lastArrivalTime;
    }

    public void setLastArrivalTime(DurationValue lastArrivalTime) {
        this.lastArrivalTime = lastArrivalTime;
    }

    @Override
    public String toString() {
        return "TraversalState{" +
                "startTime=" + startTime +
                ", startDay=" + startDay +
                ", destination=" + destination +
                ", lastArrivalTime=" + lastArrivalTime +
                '}';
    }
}
