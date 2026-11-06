package com.example.API;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootTest
@ActiveProfiles("test")
class ApiApplicationTests {

	@Autowired
	DataSource dataSource;

	@Test
	void contextLoads() throws SQLException {
		try (var connection = dataSource.getConnection()){
			assertEquals("gymsecuritytest", connection.getCatalog());
		}
	}

}
