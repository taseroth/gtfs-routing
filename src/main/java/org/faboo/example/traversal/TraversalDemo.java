package org.faboo.example.traversal;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.*;
import org.neo4j.procedure.*;

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
                    .expand(new AllExpander())
                    .evaluator(Evaluators.includeIfAcceptedByAny(new PathLogger(), new GreenEvaluator(minimumGreen)))
                    //.evaluator(new GreenEvaluator(minimumGreen))
                    ;

            final List<Node> startNodes = db.beginTx().findNodes(Label.label("Red")).stream().collect(Collectors.toList());

            final Traverser traverser = traverseDescription.traverse(startNodes);
            return StreamSupport
                    .stream(traverser.spliterator(), false)
                    .map(Path::endNode)
                    .map(NodeWrapper::new);
    }


    public static class GreenEvaluator implements Evaluator {

        private final long minimumGreen;
        private long greenFound = 0;

        public GreenEvaluator(long minimumGreen) {
            this.minimumGreen = minimumGreen;
        }

        @Override
        public Evaluation evaluate(Path path) {
            if (path.endNode().hasLabel(Label.label("Green"))) {
                greenFound++;
                if (enoughGreenFound()) {
                    System.out.println("pruning");
                    return Evaluation.INCLUDE_AND_PRUNE;
                }
                return Evaluation.INCLUDE_AND_CONTINUE;
            }
            return  Evaluation.EXCLUDE_AND_CONTINUE;
        }

        private boolean enoughGreenFound() {
            return greenFound >= minimumGreen;
        }
    }

    public static class AllExpander implements PathExpander<Integer> {


        @Override
        public Iterable<Relationship> expand(Path path, BranchState<Integer> state) {
            // expand along all relationships
            // there is a ALLExpander provided, this is here to show the interface
            return path.endNode().getRelationships(Direction.OUTGOING);
        }

        @Override
        public PathExpander<Integer> reverse() {
            return null;
        }
    }

    /**
     * Miss-using an evaluator to log out the path being evaluated.
     */
    private static class PathLogger implements Evaluator {

        @Override
        public Evaluation evaluate(Path path) {
            path.forEach(entry -> {
                if (entry instanceof Node) {
                    System.out.printf("(%s)", entry.getProperty("name"));
                } else if (entry instanceof Relationship) {
                    System.out.printf("-[%s]-", ((Relationship) entry).getType());
                }
            });
            System.out.println();
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }
    }

    public static class NodeWrapper {

        public final Node node;

        public NodeWrapper(Node node) {
            this.node = node;
        }
    }


}
