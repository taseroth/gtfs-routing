package org.faboo.example.routing.expander.filter;

import org.faboo.example.routing.JourneyBranchState;
import org.neo4j.graphdb.Relationship;

public interface StopsAtFilter {

    boolean test(Relationship stopsAt, JourneyBranchState state);
}
