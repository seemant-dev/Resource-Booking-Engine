package com.resourcebooking.server;

import com.resourcebooking.server.config.properties.CookieProperties;
import com.resourcebooking.server.config.properties.CorsProperties;
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

	@Autowired
	private CookieProperties cookieProperties;

	@Autowired
	private CorsProperties corsProperties;

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

		assertThat(cookieProperties.isSecure()).isFalse();
		assertThat(cookieProperties.getSameSite()).isEqualTo("Lax");
		assertThat(corsProperties.getAllowedOrigins())
				.containsExactly("http://localhost:3000");
	}

}
