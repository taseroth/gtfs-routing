package org.faboo.example.traversal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TraversalDemoTest {

    private static final Config driverConfig = Config.builder().withoutEncryption().build();
    private Neo4j embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(TraversalDemo.class)
                .withFixture(new File(getClass().getResource("/traversal/example_graph.cypher").getPath()))
                .build();
    }

    @Test
    void mustOnlyFind2() {

        try(
                Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
                Session session = driver.session()) {

            final List<String> names = session.run("call travers.findGreenFromRed(2)").stream()
                    .map(r -> r.get("node"))
                    .map(node -> node.get("name"))
                    .map(Value::asString)
                    .collect(Collectors.toList());
            System.out.println(names);
            assertThat(names.size()).isEqualTo(2);
            assertThat(names).allSatisfy(name -> assertThat(List.of("B2", "C1", "C2", "C3")).contains(name));
        }
    }
}
