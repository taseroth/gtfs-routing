package org.faboo.example.routing;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

public class Consts {

    public static final Label LABEL_TRIP = Label.label("Trip");
    public static final Label LABEL_STOP = Label.label("Stop");
    public static final Label LABEL_STOP_TIME = Label.label("StopTime");
    public static final String LABEL_RUNS_PREFIX = "RUNS_";

    public static final RelationshipType REL_NEXT = RelationshipType.withName("NEXT_STOP");
    public static final RelationshipType REL_STOPS = RelationshipType.withName("STOPS_AT");
    public static final RelationshipType REL_BELONGS = RelationshipType.withName("BELONGS_TO");
    public static final RelationshipType REL_USES = RelationshipType.withName("USES");

    public static final String PROP_ARRIVAL = "arrivalOffset";
    public static final String PROP_DEPART = "departureOffset";
    public static final String PROP_MIN_TRANS = "minTransferTime";
    public static final String PROP_ID = "id";
    public static final String PROP_NAME = "name";

}
