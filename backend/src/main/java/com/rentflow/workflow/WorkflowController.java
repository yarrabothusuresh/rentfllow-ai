package com.rentflow.workflow;

import com.rentflow.workflow.dto.*;
import com.rentflow.workflow.model.BookingStatus;
import com.rentflow.workflow.model.QuoteStatus;
import com.rentflow.workflow.model.WorkflowStage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "*")
public class WorkflowController {

    private final WorkflowStateMachine stateMachine;

    // In-memory demo workflow progress tracker per bookingId
    private final Map<String, WorkflowStage> bookingCurrentStageMap = new ConcurrentHashMap<>();

    public WorkflowController(WorkflowStateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @GetMapping("/demo")
    public ResponseEntity<WorkflowStatusDTO> getDemoWorkflow() {
        return getRentalWorkflow("demo-booking-001");
    }

    @GetMapping("/rental/{bookingId}")
    public ResponseEntity<WorkflowStatusDTO> getRentalWorkflow(@PathVariable String bookingId) {
        WorkflowStage currentStage = bookingCurrentStageMap.getOrDefault(bookingId, WorkflowStage.QUOTE);

        WorkflowStatusDTO statusDTO = buildWorkflowStatus(bookingId, currentStage);
        return ResponseEntity.ok(statusDTO);
    }

    @PostMapping("/rental/{bookingId}/advance")
    public ResponseEntity<WorkflowTransitionResponse> advanceWorkflow(
            @PathVariable String bookingId,
            @RequestBody(required = false) WorkflowTransitionRequest request) {

        WorkflowStage current = bookingCurrentStageMap.getOrDefault(bookingId, WorkflowStage.QUOTE);
        WorkflowStage target;

        if (request != null && request.getTargetStage() != null) {
            target = request.getTargetStage();
        } else {
            // Next ordinal in workflow sequence
            int nextOrdinal = (current.ordinal() + 1) % WorkflowStage.values().length;
            target = WorkflowStage.values()[nextOrdinal];
        }

        if (!stateMachine.isStageTransitionAllowed(current, target)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new WorkflowTransitionResponse(
                            false,
                            "Invalid workflow stage transition from " + current + " to " + target,
                            current,
                            current,
                            calculateProgress(current),
                            Instant.now().toString()
                    )
            );
        }

        bookingCurrentStageMap.put(bookingId, target);
        int newProgress = calculateProgress(target);

        return ResponseEntity.ok(new WorkflowTransitionResponse(
                true,
                "Workflow advanced successfully to " + target.getDisplayName(),
                current,
                target,
                newProgress,
                Instant.now().toString()
        ));
    }

    @PostMapping("/validate-transition/quote")
    public ResponseEntity<Map<String, Object>> validateQuoteTransition(
            @RequestParam QuoteStatus current,
            @RequestParam QuoteStatus target) {

        boolean allowed = stateMachine.isQuoteTransitionAllowed(current, target);
        Map<String, Object> response = new HashMap<>();
        response.put("current", current);
        response.put("target", target);
        response.put("allowed", allowed);
        response.put("message", allowed ? "Transition allowed" : "Transition rejected by WorkflowStateMachine");

        if (!allowed) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-transition/booking")
    public ResponseEntity<Map<String, Object>> validateBookingTransition(
            @RequestParam BookingStatus current,
            @RequestParam BookingStatus target) {

        boolean allowed = stateMachine.isBookingTransitionAllowed(current, target);
        Map<String, Object> response = new HashMap<>();
        response.put("current", current);
        response.put("target", target);
        response.put("allowed", allowed);
        response.put("message", allowed ? "Transition allowed" : "Transition rejected by WorkflowStateMachine");

        if (!allowed) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }

    private WorkflowStatusDTO buildWorkflowStatus(String bookingId, WorkflowStage currentStage) {
        WorkflowStatusDTO dto = new WorkflowStatusDTO();
        dto.setBookingId(bookingId);
        dto.setCurrentStage(currentStage);
        dto.setProgress(calculateProgress(currentStage));

        List<WorkflowStageDTO> stageDTOs = new ArrayList<>();
        WorkflowStage[] allStages = WorkflowStage.values();

        for (WorkflowStage stage : allStages) {
            String statusStr;
            if (stage.ordinal() < currentStage.ordinal()) {
                statusStr = "COMPLETED";
            } else if (stage.ordinal() == currentStage.ordinal()) {
                statusStr = "CURRENT";
            } else {
                statusStr = "PENDING";
            }

            stageDTOs.add(new WorkflowStageDTO(
                    stage.getDisplayName(),
                    stage,
                    statusStr,
                    stage.getDefaultRole(),
                    getStageDescription(stage),
                    getStageDate(stage, currentStage)
            ));
        }
        dto.setStages(stageDTOs);

        // Populate Day 5 Demo Scenario: Emily Brown Wedding
        WorkflowStatusDTO.DemoScenarioDTO scenario = new WorkflowStatusDTO.DemoScenarioDTO();
        scenario.setCustomerName("Emily Brown");
        scenario.setCustomerLocation("Dallas, Texas");
        scenario.setEventType("Wedding");
        scenario.setEventDate("September 20, 2026");
        scenario.setGuestCount(250);
        scenario.setCompanyName("Evergreen Event Rentals");
        scenario.setProducts(List.of("250 Chiavari Chairs", "25 Round Tables", "25 White Table Linens"));
        scenario.setEstimatedRental(4850.00);
        scenario.setDeliverySetup(1150.00);
        scenario.setTotalEstimated(6480.00);
        scenario.setEstimatedMargin(54.9);
        scenario.setEstimatedCost(2920.00);

        dto.setDemoScenario(scenario);

        return dto;
    }

    private int calculateProgress(WorkflowStage stage) {
        // Map 12 stages to % progress: Stage 0 -> 8%, Stage 1 -> 16%, ..., Stage 11 -> 100%
        int total = WorkflowStage.values().length;
        return (int) Math.round(((stage.ordinal() + 1) / (double) total) * 100);
    }

    private String getStageDescription(WorkflowStage stage) {
        return switch (stage) {
            case INQUIRY -> "Customer Emily Brown requested inquiry for 250-guest wedding.";
            case LEAD -> "Lead qualified by Sales; Dallas venue verified.";
            case QUOTE -> "Quote #Q-8492 ($6,480) sent to Emily Brown.";
            case BOOKING -> "Awaiting customer signature & 25% deposit.";
            case INVENTORY -> "Reserving 250 Chiavari chairs & 25 round tables.";
            case WAREHOUSE -> "Warehouse pick & pack ticket generated.";
            case DELIVERY -> "Truck #4 delivery route assigned to Dallas venue.";
            case EVENT -> "Wedding ceremony in progress at Dallas Pavilion.";
            case PICKUP -> "Driver pickup scheduled for Sept 21 08:00 AM.";
            case RETURN -> "Items returned to warehouse & inspected in bay B-4.";
            case PAYMENT -> "Final invoice payment of $6,480 settled.";
            case COMPLETED -> "Rental completed; margin 54.9% archived.";
        };
    }

    private String getStageDate(WorkflowStage stage, WorkflowStage currentStage) {
        if (stage.ordinal() <= currentStage.ordinal()) {
            return "Sep " + (10 + stage.ordinal()) + ", 2026";
        }
        return "Scheduled Sep " + (15 + stage.ordinal()) + ", 2026";
    }
}
