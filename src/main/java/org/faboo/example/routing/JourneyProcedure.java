package org.faboo.example.routing;

import org.faboo.example.traversal.TraversalDemo;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.neo4j.graphdb.traversal.Uniqueness.NODE_GLOBAL;

public class JourneyProcedure {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value = "journey.find", mode = Mode.READ)
    @Description("find journeys")
    public Stream<TraversalDemo.NodeWrapper> findGreenFromRed(@Name("startId") String startId,
                                                              @Name("destinationId") String destinationId,
                                                              @Name("startTime") LocalDateTime startTime) {

        final List<Node> startNodes = findStartNodes(startId, startTime);

        final TraversalDescription traverseDescription = db.beginTx().traversalDescription()
                .uniqueness(NODE_GLOBAL)
                .depthFirst()
                .expand(new JourneyExpander(log))
                .evaluator(new DestinationEvaluator(findDestinationNode(destinationId), log))
                ;

        log.info(String.format("starting new rout search from %s to %s, found %d starting nodes",
                startId, destinationId, startNodes.size()));

        final Traverser traverser = traverseDescription.traverse(startNodes);
        return StreamSupport
                .stream(traverser.spliterator(), false)
                .map(Path::endNode)
                .map(TraversalDemo.NodeWrapper::new);
    }

    private List<Node> findStartNodes(String startId, LocalDateTime startTime) {

        final Result result = db.beginTx().execute(
                "match (s:Stop)<-[:STOPS_AT]-(st)-[:BELONGS_TO]->(t)" +
                "where s.id = $startId and 'RUNS_' + $dt.dayOfWeek in  labels(t) " +
                "and st.departureOffset.minutes > duration({hours:$dt.hour, minutes:$dt.minute}).minutes\n" +
                "return st order by st.departureOffset asc limit 20",
                Map.of(
                        "startId", startId,
                        "dt", startTime));

        return result.stream()
                .map(e -> (Node)e.get("st"))
                .collect(Collectors.toList());
    }

    private Node findDestinationNode(String destinationId) {
        return db.beginTx().findNode(Consts.LABEL_Stop, "id", destinationId);
    }

}
