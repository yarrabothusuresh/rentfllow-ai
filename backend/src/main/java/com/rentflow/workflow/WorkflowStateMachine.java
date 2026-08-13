package com.rentflow.workflow;

import com.rentflow.workflow.model.BookingStatus;
import com.rentflow.workflow.model.QuoteStatus;
import com.rentflow.workflow.model.WorkflowStage;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class WorkflowStateMachine {

    // Valid transitions for Quote Status
    private static final Map<QuoteStatus, Set<QuoteStatus>> VALID_QUOTE_TRANSITIONS = Map.of(
            QuoteStatus.DRAFT, EnumSet.of(QuoteStatus.READY, QuoteStatus.SENT, QuoteStatus.CANCELLED),
            QuoteStatus.READY, EnumSet.of(QuoteStatus.SENT, QuoteStatus.CANCELLED),
            QuoteStatus.SENT, EnumSet.of(QuoteStatus.VIEWED, QuoteStatus.NEGOTIATION, QuoteStatus.ACCEPTED, QuoteStatus.REJECTED, QuoteStatus.EXPIRED, QuoteStatus.CANCELLED),
            QuoteStatus.VIEWED, EnumSet.of(QuoteStatus.NEGOTIATION, QuoteStatus.ACCEPTED, QuoteStatus.REJECTED, QuoteStatus.EXPIRED, QuoteStatus.CANCELLED),
            QuoteStatus.NEGOTIATION, EnumSet.of(QuoteStatus.ACCEPTED, QuoteStatus.REJECTED, QuoteStatus.CANCELLED),
            QuoteStatus.ACCEPTED, EnumSet.noneOf(QuoteStatus.class),
            QuoteStatus.REJECTED, EnumSet.noneOf(QuoteStatus.class),
            QuoteStatus.EXPIRED, EnumSet.noneOf(QuoteStatus.class),
            QuoteStatus.CANCELLED, EnumSet.noneOf(QuoteStatus.class)
    );

    // Valid transitions for Booking Status
    private static final Map<BookingStatus, Set<BookingStatus>> VALID_BOOKING_TRANSITIONS = Map.ofEntries(
            Map.entry(BookingStatus.PENDING, EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED)),
            Map.entry(BookingStatus.CONFIRMED, EnumSet.of(BookingStatus.PREPARING, BookingStatus.CANCELLED)),
            Map.entry(BookingStatus.PREPARING, EnumSet.of(BookingStatus.READY, BookingStatus.CANCELLED)),
            Map.entry(BookingStatus.READY, EnumSet.of(BookingStatus.OUT_FOR_DELIVERY, BookingStatus.CANCELLED)),
            Map.entry(BookingStatus.OUT_FOR_DELIVERY, EnumSet.of(BookingStatus.DELIVERED, BookingStatus.CANCELLED)),
            Map.entry(BookingStatus.DELIVERED, EnumSet.of(BookingStatus.EVENT_IN_PROGRESS)),
            Map.entry(BookingStatus.EVENT_IN_PROGRESS, EnumSet.of(BookingStatus.READY_FOR_PICKUP)),
            Map.entry(BookingStatus.READY_FOR_PICKUP, EnumSet.of(BookingStatus.PICKED_UP)),
            Map.entry(BookingStatus.PICKED_UP, EnumSet.of(BookingStatus.RETURNED)),
            Map.entry(BookingStatus.RETURNED, EnumSet.of(BookingStatus.INSPECTING)),
            Map.entry(BookingStatus.INSPECTING, EnumSet.of(BookingStatus.COMPLETED)),
            Map.entry(BookingStatus.COMPLETED, EnumSet.noneOf(BookingStatus.class)),
            Map.entry(BookingStatus.CANCELLED, EnumSet.noneOf(BookingStatus.class))
    );

    public boolean isQuoteTransitionAllowed(QuoteStatus current, QuoteStatus target) {
        if (current == target) return true;
        Set<QuoteStatus> allowed = VALID_QUOTE_TRANSITIONS.get(current);
        return allowed != null && allowed.contains(target);
    }

    public boolean isBookingTransitionAllowed(BookingStatus current, BookingStatus target) {
        if (current == target) return true;
        Set<BookingStatus> allowed = VALID_BOOKING_TRANSITIONS.get(current);
        return allowed != null && allowed.contains(target);
    }

    public boolean isStageTransitionAllowed(WorkflowStage current, WorkflowStage target) {
        if (current == target) return true;
        // In linear workflow sequence, allowed to step to next stage or reset to beginning
        return target.ordinal() == current.ordinal() + 1 || target.ordinal() == 0;
    }
}
