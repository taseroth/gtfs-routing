package org.faboo.example.routing;

import org.faboo.example.routing.evaluator.DestinationEvaluator;
import org.faboo.example.routing.expander.JourneyExpander;
import org.faboo.example.routing.expander.filter.DepartureTimeFilter;
import org.faboo.example.routing.expander.filter.DoubleBackFilter;
import org.faboo.example.routing.expander.filter.RunsOnFilter;
import org.faboo.example.routing.expander.filter.StopsAtFilter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.InitialBranchState;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.neo4j.graphdb.traversal.Uniqueness.NODE_GLOBAL;

public class JourneyProcedure {

    @Context
    public Transaction tx;

    @Context
    public Log log;

    @Procedure(value = "journey.find", mode = Mode.READ)
    @Description("find journeys")
    public Stream<PathWrapper> findGreenFromRed(@Name("startId") String startId,
                                                              @Name("destinationId") String destinationId,
                                                              @Name("startTime") LocalDateTime startTime) {

        try {
            log.info("** starting new search from <%s> to <%s> at <%s>", startId, destinationId, startTime);
            final List<Node> startNodes = findStartNodes(startId, startTime);

            final Node destinationStop =findDestinationNode(destinationId);

            final JourneyConfig journeyConfig = new JourneyConfig(startTime, destinationStop);
            final JourneyBranchState branchState = new JourneyBranchState();

            final JourneyExpander expander = new JourneyExpander(log, buildFilters(journeyConfig));
            DestinationEvaluator evaluator = new DestinationEvaluator(journeyConfig, log);
            final TraversalDescription traverseDescription = tx.traversalDescription()
                    .uniqueness(NODE_GLOBAL)
                    .depthFirst()
                    .expand(expander, new InitialBranchState.State<>(branchState, branchState))
                    .evaluator(evaluator)
                    ;

            log.info("starting with %d starting nodes", startNodes.size());
            System.out.printf("starting with %d starting nodes%n", startNodes.size());

            final Traverser traverser = traverseDescription.traverse(startNodes);

            final Stream<PathWrapper> stream = StreamSupport
                    .stream(traverser.spliterator(), false)
                    .map(PathWrapper::new);

            final List<PathWrapper> list = stream.collect(Collectors.toList());
            System.out.println("performed evaluations:" + journeyConfig.getEvaluationCount());
            journeyConfig.getPathsFound().forEach(p -> log.info("path: %s", p));
            return list.stream();
        } catch (NullPointerException e) {
            log.error("error during traversal", e);
        }
        return null;
    }

    private ArrayList<StopsAtFilter> buildFilters(JourneyConfig journeyConfig) {

        ArrayList<StopsAtFilter> filters = new ArrayList<>();
        filters.add(new DoubleBackFilter(log));
        filters.add(new RunsOnFilter(log, journeyConfig));
        filters.add(new DepartureTimeFilter(log));
        return filters;
    }

    private List<Node> findStartNodes(String startId, LocalDateTime startTime) {

        final Result result = tx.execute(
                "match (s:Stop)<-[:STOPS_AT]-(st)-[:BELONGS_TO]->(t)-[:USES]->(r) " +
                " where s.id = $startId and 'RUNS_' + $dt.dayOfWeek in  labels(r) " +
                " and st.departureOffset.minutes > duration({hours:$dt.hour, minutes:$dt.minute}).minutes " +
                " return st order by st.departureOffset asc limit 20",
                Map.of(
                        "startId", startId,
                        "dt", startTime));

        return result.stream()
                .map(e -> (Node)e.get("st"))
                .collect(Collectors.toList());
    }

    private Node findDestinationNode(String destinationId) {
        return tx.findNode(Consts.LABEL_STOP, "id", destinationId);
    }

    public static class PathWrapper {
        public final Path path;

        public PathWrapper(Path path) {
            this.path = path;
        }
    }
}
