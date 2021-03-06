package org.faboo.example.routing.evaluator;

import org.faboo.example.routing.Consts;
import org.faboo.example.routing.expander.TraversalState;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.PathEvaluator;
import org.neo4j.logging.Log;

public class DestinationEvaluator implements Evaluator, PathEvaluator<TraversalState> {

    private final Log log;
    private final TraversalState state;

    public DestinationEvaluator(TraversalState state, Log log) {
        this.state = state;
        this.log = log;
    }

    @Override
    public Evaluation evaluate(Path path, BranchState<TraversalState> branchState) {
        state.registerEvaluation();
        // endNode can either be a :StopTime or a :Stop
        // in case of stop, we should have evaluated it in a previous step
        final Node endNode = path.endNode();
        if (endNode.hasLabel(Consts.LABEL_STOP_TIME)) {
            final Node station = endNode.getSingleRelationship(Consts.REL_STOPS, Direction.OUTGOING).getEndNode();
            //log.info("evaluating:" + station.getProperty(Consts.PROP_NAME));
            if (state.getDestination().equals(station)) {
           //     log.info("path found:" + path);
                state.registerPath(path);
                log.info("state:" + state);
                return Evaluation.INCLUDE_AND_PRUNE;
            }
        }
        return Evaluation.EXCLUDE_AND_CONTINUE;
    }

    @Override
    public Evaluation evaluate(Path path) {
        return null;
    }
}
