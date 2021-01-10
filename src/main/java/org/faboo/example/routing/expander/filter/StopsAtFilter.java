package org.faboo.example.routing.expander.filter;

import org.faboo.example.routing.expander.TraversalState;
import org.neo4j.graphdb.Relationship;

public interface StopsAtFilter {

    boolean test(Relationship stopsAt, TraversalState state);
}
