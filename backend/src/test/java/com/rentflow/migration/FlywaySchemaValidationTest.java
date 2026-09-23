package com.rentflow.migration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Validates that the Spring Boot ApplicationContext boots cleanly when:
 * 1. Flyway executes all migrations (V1 through V5)
 * 2. Hibernate runs with ddl-auto=validate (production mode)
 *
 * This guarantees zero schema drift between JPA entity definitions and
 * Flyway migration scripts.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:validate_schema_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:db/migration",
    "spring.flyway.baseline-on-migrate=false",
    "spring.flyway.validate-on-migrate=true",
    "spring.jpa.hibernate.ddl-auto=validate"
})
public class FlywaySchemaValidationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Application Context boots with Flyway V1-V5 and Hibernate ddl-auto=validate")
    void testContextLoadsWithFlywayAndHibernateValidation() {
        assertNotNull(applicationContext, "ApplicationContext must be successfully initialized");
    }
}
