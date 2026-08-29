package com.rentflow.calendar.controller;

import com.rentflow.calendar.dto.*;
import com.rentflow.calendar.model.CalendarEventType;
import com.rentflow.calendar.model.OperationalConflict;
import com.rentflow.calendar.service.CalendarService;
import com.rentflow.security.SecurityUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCalendar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) List<CalendarEventType> eventTypes,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID warehouseId) {

        String tenantId = SecurityUtils.getCurrentTenantId();

        List<CalendarEventDTO> events = calendarService.getCalendarEvents(
                tenantId, start, end, eventTypes, resourceType, resourceId, status, warehouseId);

        Map<String, Object> response = new HashMap<>();
        response.put("events", events);
        response.put("total", events.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reschedule")
    public ResponseEntity<CalendarEventDTO> rescheduleEvent(
            @RequestBody RescheduleRequestDTO request,
            Principal principal) {

        String tenantId = SecurityUtils.getCurrentTenantId();
        String username = principal != null ? principal.getName() : "Operations Manager";
        CalendarEventDTO updated = calendarService.rescheduleEvent(tenantId, request, username);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<CalendarDashboardDTO> getDashboardMetrics() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        CalendarDashboardDTO metrics = calendarService.getDashboardMetrics(tenantId);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/conflicts")
    public ResponseEntity<List<OperationalConflict>> getConflicts() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        List<OperationalConflict> conflicts = calendarService.getConflicts(tenantId);
        return ResponseEntity.ok(conflicts);
    }

    @PostMapping("/conflicts/{id}/override")
    public ResponseEntity<OperationalConflict> overrideConflict(
            @PathVariable UUID id,
            @RequestBody ConflictOverrideRequestDTO request,
            Principal principal) {

        String tenantId = SecurityUtils.getCurrentTenantId();
        String username = principal != null ? principal.getName() : "Operations Manager";
        String reason = request != null ? request.getReason() : "Manager override approved";
        OperationalConflict overridden = calendarService.overrideConflict(tenantId, id, reason, username);
        return ResponseEntity.ok(overridden);
    }
}
