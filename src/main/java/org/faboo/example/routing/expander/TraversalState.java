package org.faboo.example.routing.expander;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.values.storable.DurationValue;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class TraversalState {

    private final LocalDateTime startTime;
    private final DayOfWeek startDay;
    private final Node destination;
    private DurationValue lastArrivalTime;
    private long evaluationCount;
    private final List<Path> pathsFound;

    public TraversalState(LocalDateTime startTime,  Node destination) {
        this.startTime = startTime;
        this.startDay = startTime.getDayOfWeek();
        this.destination = destination;
        this.evaluationCount = 0;
        this.pathsFound = new ArrayList<>();
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

    public void registerPath(Path path) {
        pathsFound.add(path);
    }

    public Collection<Path> getPathsFound() {
        return Collections.unmodifiableList(pathsFound);
    }

    public void registerEvaluation() {
        evaluationCount++;
    }
    public long getEvaluationCount() {
        return evaluationCount;
    }

    @Override
    public String toString() {
        return "TraversalState{" +
                "lastArrivalTime=" + lastArrivalTime +
                ", evaluationCount=" + evaluationCount +
                ", pathsFound=" + pathsFound.size() +
                '}';
    }
}
