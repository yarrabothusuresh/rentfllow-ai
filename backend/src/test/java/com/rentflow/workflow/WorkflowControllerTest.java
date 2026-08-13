package com.rentflow.workflow;

import com.rentflow.workflow.dto.WorkflowStatusDTO;
import com.rentflow.workflow.dto.WorkflowTransitionResponse;
import com.rentflow.workflow.model.BookingStatus;
import com.rentflow.workflow.model.QuoteStatus;
import com.rentflow.workflow.model.WorkflowStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowControllerTest {

    private WorkflowStateMachine stateMachine;
    private WorkflowController workflowController;

    @BeforeEach
    void setUp() {
        stateMachine = new WorkflowStateMachine();
        workflowController = new WorkflowController(stateMachine);
    }

    @Test
    void testGetDemoWorkflow() {
        ResponseEntity<WorkflowStatusDTO> response = workflowController.getDemoWorkflow();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("demo-booking-001", response.getBody().getBookingId());
        assertEquals(WorkflowStage.QUOTE, response.getBody().getCurrentStage());
        assertEquals(12, response.getBody().getStages().size());

        // Check demo scenario
        assertNotNull(response.getBody().getDemoScenario());
        assertEquals("Emily Brown", response.getBody().getDemoScenario().getCustomerName());
        assertEquals(6480.0, response.getBody().getDemoScenario().getTotalEstimated());
    }

    @Test
    void testAdvanceWorkflow() {
        ResponseEntity<WorkflowTransitionResponse> response = workflowController.advanceWorkflow("demo-booking-001", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(WorkflowStage.BOOKING, response.getBody().getCurrentStage());
    }

    @Test
    void testQuoteTransitionValidation() {
        // Valid: DRAFT -> SENT
        ResponseEntity<Map<String, Object>> validResponse = workflowController.validateQuoteTransition(QuoteStatus.DRAFT, QuoteStatus.SENT);
        assertEquals(HttpStatus.OK, validResponse.getStatusCode());
        assertTrue((Boolean) validResponse.getBody().get("allowed"));

        // Invalid: ACCEPTED -> DRAFT
        ResponseEntity<Map<String, Object>> invalidResponse = workflowController.validateQuoteTransition(QuoteStatus.ACCEPTED, QuoteStatus.DRAFT);
        assertEquals(HttpStatus.BAD_REQUEST, invalidResponse.getStatusCode());
        assertFalse((Boolean) invalidResponse.getBody().get("allowed"));
    }

    @Test
    void testBookingTransitionValidation() {
        // Valid: PENDING -> CONFIRMED
        ResponseEntity<Map<String, Object>> validResponse = workflowController.validateBookingTransition(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        assertEquals(HttpStatus.OK, validResponse.getStatusCode());
        assertTrue((Boolean) validResponse.getBody().get("allowed"));

        // Invalid: COMPLETED -> PENDING
        ResponseEntity<Map<String, Object>> invalidResponse = workflowController.validateBookingTransition(BookingStatus.COMPLETED, BookingStatus.PENDING);
        assertEquals(HttpStatus.BAD_REQUEST, invalidResponse.getStatusCode());
        assertFalse((Boolean) invalidResponse.getBody().get("allowed"));
    }
}
