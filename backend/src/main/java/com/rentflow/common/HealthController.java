package com.rentflow.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standardized, safe health and readiness endpoints for Docker/Kubernetes container probes.
 * Never leaks internal environment variables, database hosts, credentials, or stack traces.
 */
@RestController
@RequestMapping("/api/health")
@CrossOrigin(originPatterns = "*")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    @Autowired(required = false)
    private DataSource dataSource;

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("application", "RentFlow AI");
        body.put("status", "UP");
        body.put("version", "0.1.0");
        body.put("timestamp", Instant.now().toString());
        return body;
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());

        boolean dbOk = checkDatabase();
        if (dbOk) {
            body.put("status", "READY");
            body.put("database", "CONNECTED");
            return ResponseEntity.ok(body);
        } else {
            body.put("status", "NOT_READY");
            body.put("database", "DISCONNECTED");
            log.warn("Readiness check failed: Database connection unavailable.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }
    }

    private boolean checkDatabase() {
        if (dataSource == null) {
            return false;
        }
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.execute("SELECT 1");
        } catch (Exception e) {
            log.warn("Health database check failed: {}", e.getMessage());
            return false;
        }
    }
}
