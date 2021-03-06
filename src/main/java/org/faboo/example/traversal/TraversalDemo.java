package org.faboo.example.traversal;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.*;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.neo4j.graphdb.traversal.Uniqueness.NODE_GLOBAL;

public class TraversalDemo {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "travers.findGreenFromRed", mode = Mode.READ)
    @Description("traverses from :Red nodes until it finds minimumGreen :Green nodes")
    public Stream<NodeWrapper> findGreenFromRed(@Name("minimumGreen") Long minimumGreen) {


            final TraversalDescription traverseDescription = db.beginTx().traversalDescription()
                    .uniqueness(NODE_GLOBAL)
                    .depthFirst()
                    .expand(new AllExpander(), new InitialBranchState.State<>(new ArrayList<>(), new ArrayList<>()))
                    .evaluator(Evaluators.includeIfAcceptedByAny(new LoggingEvaluators(), new GreenEvaluator(minimumGreen)))
                    //.evaluator(new GreenEvaluator(minimumGreen))
                    ;

            final List<Node> startNodes = db.beginTx().findNodes(Label.label("Red")).stream().collect(Collectors.toList());

            final Traverser traverser = traverseDescription.traverse(startNodes);
            return StreamSupport
                    .stream(traverser.spliterator(), false)
                    .map(Path::endNode)
                    .map(NodeWrapper::new);
    }


    public static class GreenEvaluator implements PathEvaluator<String> {

        private final long minimumGreen;
        private long greenFound = 0;

        public GreenEvaluator(long minimumGreen) {
            this.minimumGreen = minimumGreen;
        }

        @Override
        public Evaluation evaluate(Path path, BranchState<String> branchState) {

            if (path.endNode().hasLabel(Label.label("Green"))) {
                greenFound++;
                if (enoughGreenFound()) {
                    return Evaluation.EXCLUDE_AND_PRUNE;
                } else {
                    return Evaluation.INCLUDE_AND_CONTINUE;
                }
            } else if (enoughGreenFound()) {
                return Evaluation.EXCLUDE_AND_PRUNE;
            } else{
                return  Evaluation.EXCLUDE_AND_CONTINUE;
            }
        }

        private boolean enoughGreenFound() {
            return greenFound > minimumGreen;
        }

        @Override
        public Evaluation evaluate(Path path) {
            return null;
        }
    }

    public static class AllExpander implements PathExpander<List<String>> {

        @Override
        public Iterable<Relationship> expand(Path path, BranchState<List<String>> branchState) {
            PathLogger.logPath(path, "exp", branchState);
            // expand along all relationships
            // there is a ALLExpander provided, this is here to show the interface
            final ArrayList<String> newState = new ArrayList<>(branchState.getState());
            newState.add((String)path.endNode().getProperty("name"));
            branchState.setState(newState);
            return path.endNode().getRelationships(Direction.OUTGOING);
        }

        @Override
        public PathExpander<List<String>> reverse() {
            return null;
        }
    }

    public static class PathLogger {

        public static void logPath(Path path, String scope, BranchState<List<String>> branchState) {
            System.out.print(scope + "\t: " + branchState.getState() + " \t:");
            path.forEach(entry -> {
                if (entry instanceof Node) {
                    System.out.printf("(%s)", entry.getProperty("name"));
                } else if (entry instanceof Relationship) {
                    System.out.printf("-[%s]-", ((Relationship) entry).getType());
                }
            });
            System.out.println();
        }
    }

    /**
     * Miss-using an evaluator to log out the path being evaluated.
     */
    private static class LoggingEvaluators implements PathEvaluator<List<String>> {

        @Override
        public Evaluation evaluate(Path path, BranchState<List<String>>branchState) {
            PathLogger.logPath(path, "eva", branchState);
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }

        @Override
        public Evaluation evaluate(Path path) {
            return null;
        }
    }

    public static class NodeWrapper {

        public final Node node;

        public NodeWrapper(Node node) {
            this.node = node;
        }
    }


}
