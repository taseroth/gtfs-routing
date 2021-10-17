package org.faboo.example.routing.evaluator;

import org.faboo.example.routing.Consts;
import org.faboo.example.routing.JourneyBranchState;
import org.faboo.example.routing.JourneyConfig;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.PathEvaluator;
import org.neo4j.logging.Log;

public class DestinationEvaluator implements Evaluator, PathEvaluator<JourneyBranchState> {

    private final Log log;
    private final JourneyConfig state;

    public DestinationEvaluator(JourneyConfig state, Log log) {
        this.state = state;
        this.log = log;
    }

    @Override
    public Evaluation evaluate(Path path, BranchState<JourneyBranchState> branchState) {
        System.out.println(String.format("eva: %s state:%s", path, branchState.getState()));
        state.registerEvaluation();
        // endNode can either be a :StopTime or a :Stop
        // in case of stop, we should have evaluated it in a previous step
        final Node endNode = path.endNode();
        if (endNode.hasLabel(Consts.LABEL_STOP_TIME)) {
            final Node stop = endNode.getSingleRelationship(Consts.REL_STOPS, Direction.OUTGOING).getEndNode();
            //log.info("evaluating:" + stop.getProperty(Consts.PROP_NAME));
            if (state.getDestination().equals(stop)) {
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
