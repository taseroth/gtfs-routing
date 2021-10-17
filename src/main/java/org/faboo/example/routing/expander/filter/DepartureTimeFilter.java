package org.faboo.example.routing.expander.filter;

import org.faboo.example.routing.Consts;
import org.faboo.example.routing.JourneyBranchState;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;
import org.neo4j.values.storable.DurationValue;

public class DepartureTimeFilter implements StopsAtFilter {

    private final Log log;

    public DepartureTimeFilter(Log log) {
        this.log = log;
    }

    @Override
    public boolean test(Relationship relationship, JourneyBranchState state) {
        // relationship must be of type :STOPS_AT
        DurationValue minTransfer = (DurationValue)relationship.getEndNode().getProperty(Consts.PROP_MIN_TRANS, 0);
        DurationValue earliest = state.getLastArrivalTime().add(minTransfer);
        DurationValue departure = (DurationValue)relationship.getStartNode().getProperty(Consts.PROP_DEPART);
        //log.debug("testing %s >= %s", departure, earliest);
        return departure.compareTo(earliest) >= 0;
    }
}
