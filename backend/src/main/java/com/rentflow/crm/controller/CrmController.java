package com.rentflow.crm.controller;

import com.rentflow.security.SecurityUtils;
import com.rentflow.crm.dto.*;
import com.rentflow.crm.model.ActivityDirection;
import com.rentflow.crm.model.ActivityType;
import com.rentflow.crm.model.CallOutcome;
import com.rentflow.crm.model.LeadStage;
import com.rentflow.crm.service.CrmDashboardService;
import com.rentflow.crm.service.LeadActivityService;
import com.rentflow.crm.service.LeadFollowUpService;
import com.rentflow.crm.service.LeadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/crm")
public class CrmController {

    private final LeadService leadService;
    private final LeadActivityService activityService;
    private final LeadFollowUpService followUpService;
    private final CrmDashboardService dashboardService;

    public CrmController(LeadService leadService,
                         LeadActivityService activityService,
                         LeadFollowUpService followUpService,
                         CrmDashboardService dashboardService) {
        this.leadService = leadService;
        this.activityService = activityService;
        this.followUpService = followUpService;
        this.dashboardService = dashboardService;
    }

    private String getTenantId() {
        return SecurityUtils.getCurrentTenantId();
    }

    private String getCurrentUser() {
        return SecurityUtils.getCurrentUser();
    }

    // --- DASHBOARD & PIPELINE ---

    @GetMapping("/dashboard")
    public ResponseEntity<CrmDashboardDTO> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard(getTenantId(), getCurrentUser()));
    }

    @GetMapping("/pipeline")
    public ResponseEntity<CrmPipelineDTO> getPipeline() {
        return ResponseEntity.ok(dashboardService.getPipeline(getTenantId()));
    }

    // --- LEADS CRUD & SEARCH ---

    @GetMapping("/leads")
    public ResponseEntity<Page<LeadSummaryDTO>> getLeads(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) LeadStage stage,
            @RequestParam(required = false) String assignedUserId,
            @RequestParam(defaultValue = "false") boolean unassignedOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Page<LeadSummaryDTO> result = leadService.searchLeads(
                getTenantId(), query, stage, assignedUserId, unassignedOnly, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/leads")
    public ResponseEntity<LeadDetailDTO> createLead(@RequestBody LeadCreateRequest req) {
        return ResponseEntity.ok(leadService.createLead(getTenantId(), req, getCurrentUser()));
    }

    @GetMapping("/leads/{id}")
    public ResponseEntity<LeadDetailDTO> getLeadById(@PathVariable UUID id) {
        return leadService.getLeadById(getTenantId(), id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/leads/{id}")
    public ResponseEntity<LeadDetailDTO> updateLead(@PathVariable UUID id, @RequestBody LeadUpdateRequest req) {
        return ResponseEntity.ok(leadService.updateLead(getTenantId(), id, req, getCurrentUser()));
    }

    // --- LIFECYCLE & TRANSITIONS ---

    @PostMapping("/leads/{id}/assign")
    public ResponseEntity<LeadDetailDTO> assignLead(@PathVariable UUID id, @RequestBody LeadAssignRequest req) {
        return ResponseEntity.ok(leadService.assignLead(getTenantId(), id, req, getCurrentUser()));
    }

    @PostMapping("/leads/{id}/transition")
    public ResponseEntity<LeadDetailDTO> transitionStage(@PathVariable UUID id, @RequestBody LeadTransitionRequest req) {
        return ResponseEntity.ok(leadService.transitionStage(getTenantId(), id, req, getCurrentUser()));
    }

    @PostMapping("/leads/{id}/qualify")
    public ResponseEntity<LeadDetailDTO> qualifyLead(@PathVariable UUID id) {
        return ResponseEntity.ok(leadService.qualifyLead(getTenantId(), id, getCurrentUser()));
    }

    @PostMapping("/leads/{id}/disqualify")
    public ResponseEntity<LeadDetailDTO> disqualifyLead(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : "Not a fit";
        return ResponseEntity.ok(leadService.disqualifyLead(getTenantId(), id, reason, getCurrentUser()));
    }

    @PostMapping("/leads/{id}/won")
    public ResponseEntity<LeadDetailDTO> markWon(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        UUID quoteId = (body != null && body.get("quoteId") != null) ? UUID.fromString(body.get("quoteId")) : null;
        UUID bookingId = (body != null && body.get("bookingId") != null) ? UUID.fromString(body.get("bookingId")) : null;
        return ResponseEntity.ok(leadService.markWon(getTenantId(), id, quoteId, bookingId, getCurrentUser()));
    }

    @PostMapping("/leads/{id}/lost")
    public ResponseEntity<LeadDetailDTO> markLost(@PathVariable UUID id, @RequestBody LeadLostRequest req) {
        return ResponseEntity.ok(leadService.markLost(getTenantId(), id, req, getCurrentUser()));
    }

    @PostMapping("/leads/{id}/reopen")
    public ResponseEntity<LeadDetailDTO> reopenLead(@PathVariable UUID id, @RequestBody LeadReopenRequest req) {
        return ResponseEntity.ok(leadService.reopenLead(getTenantId(), id, req, getCurrentUser()));
    }

    // --- CUSTOMER & QUOTE ACTIONS ---

    @GetMapping("/leads/{id}/matches")
    public ResponseEntity<List<CustomerMatchDTO>> getCustomerMatches(@PathVariable UUID id) {
        return ResponseEntity.ok(leadService.checkCustomerMatches(getTenantId(), id));
    }

    @PostMapping("/leads/{id}/link-customer")
    public ResponseEntity<LeadDetailDTO> linkCustomer(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        UUID customerId = UUID.fromString(body.get("customerId"));
        return ResponseEntity.ok(leadService.linkCustomer(getTenantId(), id, customerId, getCurrentUser()));
    }

    @PostMapping("/leads/{id}/convert-customer")
    public ResponseEntity<LeadDetailDTO> convertCustomer(@PathVariable UUID id, @RequestBody(required = false) Map<String, Boolean> body) {
        boolean forceNew = body != null && Boolean.TRUE.equals(body.get("forceNew"));
        return ResponseEntity.ok(leadService.convertCustomer(getTenantId(), id, forceNew, getCurrentUser()));
    }

    @PostMapping("/leads/{id}/quote")
    public ResponseEntity<LeadDetailDTO> createQuoteDraft(@PathVariable UUID id) {
        return ResponseEntity.ok(leadService.createQuoteDraftFromLead(getTenantId(), id, getCurrentUser()));
    }

    // --- ACTIVITIES & TIMELINE ---

    @GetMapping("/leads/{id}/activities")
    public ResponseEntity<List<LeadActivityDTO>> getActivities(@PathVariable UUID id) {
        return ResponseEntity.ok(activityService.getActivities(getTenantId(), id));
    }

    @PostMapping("/leads/{id}/activities")
    public ResponseEntity<LeadActivityDTO> logActivity(@PathVariable UUID id, @RequestBody LeadActivityDTO dto) {
        ActivityType type = dto.getType() != null ? dto.getType() : ActivityType.NOTE;
        ActivityDirection direction = dto.getDirection() != null ? dto.getDirection() : ActivityDirection.INTERNAL;
        CallOutcome outcome = dto.getCallOutcome();

        LeadActivityDTO logged = activityService.logActivity(getTenantId(), id, type, direction,
                dto.getSubject() != null ? dto.getSubject() : "Activity Logged",
                dto.getSummary(), dto.getNotes(), outcome, dto.getReferenceType(), dto.getReferenceId(), getCurrentUser());
        return ResponseEntity.ok(logged);
    }

    // --- FOLLOW-UPS / TASKS ---

    @GetMapping("/leads/{id}/follow-ups")
    public ResponseEntity<List<LeadFollowUpDTO>> getFollowUps(@PathVariable UUID id) {
        return ResponseEntity.ok(followUpService.getFollowUps(getTenantId(), id));
    }

    @PostMapping("/leads/{id}/follow-ups")
    public ResponseEntity<LeadFollowUpDTO> scheduleFollowUp(@PathVariable UUID id, @RequestBody LeadFollowUpDTO dto) {
        LeadFollowUpDTO created = followUpService.scheduleFollowUp(getTenantId(), id, dto.getType(),
                dto.getTitle(), dto.getNotes(), dto.getDueAt(), dto.getAssignedTo(), dto.getAssignedToName(), getCurrentUser());
        return ResponseEntity.ok(created);
    }

    @PostMapping("/follow-ups/{followUpId}/complete")
    public ResponseEntity<LeadFollowUpDTO> completeFollowUp(@PathVariable UUID followUpId) {
        return ResponseEntity.ok(followUpService.completeFollowUp(getTenantId(), followUpId, getCurrentUser()));
    }

    @PostMapping("/follow-ups/{followUpId}/cancel")
    public ResponseEntity<LeadFollowUpDTO> cancelFollowUp(@PathVariable UUID followUpId) {
        return ResponseEntity.ok(followUpService.cancelFollowUp(getTenantId(), followUpId, getCurrentUser()));
    }
}
