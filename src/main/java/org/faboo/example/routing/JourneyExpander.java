package org.faboo.example.routing;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.logging.Log;

public class JourneyExpander implements PathExpander<Integer> {

    private final Log log;

    public JourneyExpander(Log log) {
        this.log = log;
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState<Integer> state) {
        log.info("expanding: " + path);
        final Node endNode = path.endNode();
        return endNode.getRelationships(Direction.OUTGOING, Consts.REL_NEXT);
    }

    @Override
    public PathExpander<Integer> reverse() {
        return null;
    }
}
