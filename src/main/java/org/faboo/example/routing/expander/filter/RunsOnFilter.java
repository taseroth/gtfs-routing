package org.faboo.example.routing.expander.filter;

import org.faboo.example.routing.Consts;
import org.faboo.example.routing.JourneyBranchState;
import org.faboo.example.routing.JourneyConfig;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;

public class RunsOnFilter implements StopsAtFilter {

    private final Log log;
    private final JourneyConfig journeyConfig;

    public RunsOnFilter(Log log, JourneyConfig journeyConfig) {
        this.log = log;
        this.journeyConfig = journeyConfig;
    }

    @Override
    public boolean test(Relationship relationship, JourneyBranchState state) {
        //log.info("testing %s with state:%s", relationship, state);
        return relationship.getStartNode() // :StopTime
                .getSingleRelationship(Consts.REL_BELONGS, Direction.OUTGOING).getEndNode() // :Trip
                .getSingleRelationship(Consts.REL_USES, Direction.OUTGOING).getEndNode() // :Route
        .hasLabel(calcRunOn(state));
    }

    private Label calcRunOn(JourneyBranchState state) {
        // TODO: add handling of overnight trips
        return Label.label(Consts.LABEL_RUNS_PREFIX + journeyConfig.getStartDay().getValue());
    }
}
