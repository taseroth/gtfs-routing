package org.faboo.example.routing;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Path;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JourneyProcedureTest {

    private static final Config driverConfig = Config.builder().withoutEncryption().build();
    private Neo4j embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withProcedure(JourneyProcedure.class)
                .withFixture(new File(getClass().getResource("/routing/sample.cypher").getPath()))
                .build();
    }

    @Test
    void name() {

        try(
                Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
                Session session = driver.session()) {

            final List<Path> paths =
                    session.run("call journey.find('S1', 'S2', localdatetime('2021-01-09T08:00:00')) yield path return path")
                            .stream()
                            .map(r -> r.get("path"))
                            .map(Value::asPath)
                    .collect(Collectors.toList());
            System.out.println(paths);
            assertThat(paths.size()).isEqualTo(2);
        }
    }

}
