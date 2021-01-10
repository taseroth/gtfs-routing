package org.faboo.example.routing.expander;

import org.faboo.example.routing.Consts;
import org.faboo.example.routing.expander.filter.StopsAtFilter;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.logging.Log;
import org.neo4j.values.storable.DurationValue;

import java.util.ArrayList;
import java.util.Collection;

public class JourneyExpander implements PathExpander<TraversalState> {

    private final Log log;
    private final ArrayList<StopsAtFilter> stopAtFilters;
    public JourneyExpander(Log log, ArrayList<StopsAtFilter> stopAtFilters) {
        this.log = log;
        this.stopAtFilters = stopAtFilters;
        log.info("constructing expander with %d filter ", stopAtFilters.size());
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState<TraversalState> state) {
        log.info("expanding: " + path);
        // endNode can either be
        // :StopTime : expand along the same trip, using the NEXT_STOP rel
        // :STOP find the next possible / likely trips to hop on. This is where all the magic will happen.
        return buildExpansion(path, state);
    }

    @Override
    public PathExpander<TraversalState> reverse() {
        return null;
    }

    private Collection<Relationship> buildExpansion(Path path, BranchState<TraversalState> state) {

        Collection<Relationship> result = new ArrayList<>();
        final Node endNode = path.endNode();

        if (endNode.hasLabel(Consts.LABEL_STOP_TIME)) {
            result.add(endNode.getSingleRelationship(Consts.REL_NEXT, Direction.OUTGOING));
            result.add(endNode.getSingleRelationship(Consts.REL_STOPS, Direction.OUTGOING));
            state.getState().setLastArrivalTime((DurationValue)endNode.getProperty(Consts.PROP_ARRIVAL));
            state.setState(state.getState());
        } else if (endNode.hasLabel(Consts.LABEL_STOP)) {
            result.addAll(findInterchangeTrips(endNode, state.getState()));
        }
        log.info("adding " + result.size() + " to expansion list");
        return result;
    }

    private Collection<Relationship> findInterchangeTrips(Node stop, TraversalState state) {

        Collection<Relationship> result = new ArrayList<>();

        for (Relationship rel : stop.getRelationships(Direction.INCOMING, Consts.REL_STOPS)) {
            for (StopsAtFilter filter : stopAtFilters) {
                if (filter.test(rel, state)) {
                    result.add(rel);
                }
            }
        }

        return result;
    }
}
