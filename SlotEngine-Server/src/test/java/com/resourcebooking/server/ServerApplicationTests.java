package com.resourcebooking.server;

import com.resourcebooking.server.integration.AbstractPostgresIntegrationTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ServerApplicationTests extends AbstractPostgresIntegrationTest {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Flyway flyway;

	@Test
	void contextLoads() throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			assertThat(connection.getMetaData().getURL())
					.isEqualTo(POSTGRES.getJdbcUrl())
					.doesNotContain("localhost:5433");
		}

		assertThat(Arrays.stream(flyway.info().applied())
				.map(migration -> migration.getVersion().getVersion())
				.toList())
				.containsExactlyInAnyOrder("1", "2", "3", "4");
	}

}
