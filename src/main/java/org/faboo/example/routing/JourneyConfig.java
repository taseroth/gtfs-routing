package org.faboo.example.routing;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Keeps data that is independent of a specific branch during expansion / evaluation.
 */
public class JourneyConfig {

    private final LocalDateTime startTime;
    private final DayOfWeek startDay;
    private final Node destination;
    private long evaluationCount;
    private final List<Path> pathsFound;

    public JourneyConfig(LocalDateTime startTime, Node destination) {
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
                "evaluationCount=" + evaluationCount +
                ", pathsFound=" + pathsFound.size() +
                '}';
    }
}
