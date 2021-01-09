package org.faboo.example.routing;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.PathEvaluator;
import org.neo4j.logging.Log;

public class DestinationEvaluator implements Evaluator, PathEvaluator<Integer> {

    private final Node destination;
    private final Log log;

    public DestinationEvaluator(Node destinationNode, Log log) {
        destination = destinationNode;
        this.log = log;
    }

    @Override
    public Evaluation evaluate(Path path, BranchState state) {
        final Node station = path.endNode().getSingleRelationship(Consts.REL_STOPS, Direction.OUTGOING).getEndNode();
        log.info("evaluating:" + station.getProperty("name"));
        if (destination.equals(station)) {
            return Evaluation.INCLUDE_AND_PRUNE;
        }
        return Evaluation.EXCLUDE_AND_CONTINUE;
    }

    @Override
    public Evaluation evaluate(Path path) {
        return null;
    }
}
