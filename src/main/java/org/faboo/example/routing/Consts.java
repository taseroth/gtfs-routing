package org.faboo.example.routing;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

public class Consts {

    public static final Label LABEL_TRIP = Label.label("Trip");
    public static final Label LABEL_Stop = Label.label("Stop");

    public static final RelationshipType REL_NEXT = RelationshipType.withName("NEXT_STOP");
    public static final RelationshipType REL_STOPS = RelationshipType.withName("STOPS_AT");
}
