package org.faboo.example.routing.expander.filter;

import org.faboo.example.routing.JourneyBranchState;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;

public class DoubleBackFilter implements StopsAtFilter {

    private final Log log;

    public DoubleBackFilter(Log log) {
        this.log = log;
    }

    @Override
    public boolean test(Relationship stopsAt, JourneyBranchState state) {
        System.out.println(state);
        System.out.println("registering: " + stopsAt);
        final boolean b = state.registerStop(stopsAt.getEndNode());
        if (!b) {
            System.out.println("preventing double back at: " + stopsAt.getEndNode().getProperty("id"));
        }
        return b;
    }
}
