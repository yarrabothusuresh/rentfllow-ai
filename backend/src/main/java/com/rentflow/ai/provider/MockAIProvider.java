package com.rentflow.ai.provider;

import com.rentflow.ai.dto.AIRequest;
import com.rentflow.ai.dto.AIResponse;
import com.rentflow.ai.dto.ToolResult;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MockAIProvider implements AIProvider {

    @Override
    public String getProviderName() {
        return "MockAIProvider";
    }

    @Override
    public AIResponse generate(AIRequest request, String intent, List<String> toolsUsed, List<ToolResult> toolResults, List<String> reasoningSteps) {
        AIResponse response = new AIResponse();
        response.setIntent(intent);
        response.setToolsUsed(toolsUsed != null ? toolsUsed : new ArrayList<>());
        response.setReasoningSteps(reasoningSteps != null ? reasoningSteps : new ArrayList<>());

        // Check if any tool resulted in PERMISSION_DENIED or ACTION_REQUIRES_APPROVAL
        for (ToolResult tr : toolResults) {
            if ("PERMISSION_DENIED".equals(tr.getStatus())) {
                response.setIntent("PERMISSION_DENIED");
                response.setMessage("You don't have permission to access that information.");
                response.setSuggestedActions(List.of("Contact Admin", "View Storefront"));
                response.setRequiresApproval(false);
                return response;
            }
            if ("ACTION_REQUIRES_APPROVAL".equals(tr.getStatus())) {
                response.setIntent("ACTION_REQUIRES_APPROVAL");
                response.setRequiresApproval(true);
                response.setMessage(tr.getMessage());
                if (tr.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> details = (Map<String, Object>) tr.getData();
                    response.setActionDetails(details);
                }
                response.setSuggestedActions(List.of("Approve Action", "Cancel"));
                return response;
            }
        }

        // Tool-driven response override for Day 8 Product & Inventory tools
        if (List.of("AVAILABILITY_CHECK", "PRODUCT_INQUIRY", "LOW_STOCK_INQUIRY").contains(intent)) {
            for (int i = toolResults.size() - 1; i >= 0; i--) {
                ToolResult tr = toolResults.get(i);
                if (tr.getMessage() != null && !tr.getMessage().isBlank() && tr.getData() != null) {
                    response.setMessage(tr.getMessage());
                    response.setSuggestedActions(List.of("View Product", "Check Availability", "View Event"));
                    response.setRequiresApproval(false);
                    return response;
                }
            }
        }

        // Standard Intent Responses
        switch (intent) {
            case "QUOTE_CREATION":
                response.setMessage("Created DRAFT quote QUO-000001 for Emily Brown (Emily's Wedding).\nItems: 250 Chiavari Chairs, 25 Round Tables, 25 White Linens, 10 LED Uplights.\nSubtotal: $2,925.00 | Discount (10%): -$292.50 | Fees: $500.00 | Tax (8.25%): $258.49 | Total: $3,390.99 | Deposit Required: $1,017.30");
                response.setSuggestedActions(List.of("View Quote", "Send Quote to Customer"));
                response.setRequiresApproval(false);
                break;

            case "QUOTE_DISCOUNT":
                response.setMessage("Applying a 10% discount to Emily's Wedding quote reduces subtotal by $292.50. Proposed Total: $3,390.99 (Deposit: $1,017.30, Remaining: $2,373.69).");
                response.setSuggestedActions(List.of("Apply Discount", "Recalculate Quote"));
                response.setRequiresApproval(false);
                break;

            case "CUSTOMER_SEARCH_RESULT":
                response.setMessage("Found customer: Emily Brown (CUS-000001).\nEmail: emily.brown@example-demo.com | Phone: +1 555-010-1001\nUpcoming event: Emily's Wedding (September 20, 2026, 250 guests at Dallas Garden Hall).");
                response.setSuggestedActions(List.of("View Customer", "View Event", "Create Quote"));
                response.setRequiresApproval(false);
                break;

            case "EVENT_STATUS_DETAILS":
                response.setMessage("Emily Brown's wedding is currently in PLANNING status for September 20, 2026.\n\nGuest count: 250\nVenue: Dallas Garden Hall\n\nThe next expected step is to create a rental quote.");
                response.setSuggestedActions(List.of("View Event", "Check Product Availability", "Create Quote"));
                response.setRequiresApproval(false);
                break;

            case "UPCOMING_EVENTS":
                response.setMessage("Upcoming scheduled events for Evergreen Event Rentals:\n1. Emily's Wedding - September 20, 2026 (250 guests, Dallas Garden Hall)\n2. Fairview Autumn Gala - September 21, 2026 (120 guests, Fairview Event Hall)\n3. TechCorp Annual Gala - September 22, 2026 (500 guests, Austin Convention Center)");
                response.setSuggestedActions(List.of("View Events Calendar", "View Staging Schedule"));
                response.setRequiresApproval(false);
                break;

            case "CUSTOMER_BOOKING_STATUS":
                response.setMessage("Emily Brown's wedding quote ($3,390.99) has been created and is in DRAFT status. 250 Chiavari chairs and 25 round tables are currently available.");
                response.setSuggestedActions(List.of("View Rental Workflow", "Send Follow-up"));
                response.setRequiresApproval(false);
                break;

            case "AVAILABILITY_CHECK":
                response.setMessage("Yes! You have 465 Chiavari chairs available out of 500 total inventory for September 20, 2026. Required quantity: 250 chairs.");
                response.setSuggestedActions(List.of("Reserve Inventory", "View Products"));
                response.setRequiresApproval(false);
                break;

            case "PROFITABILITY_ANALYSIS":
                response.setMessage("The TechCorp Annual Gala on Sept 22 is expected to yield the highest dollar profit ($7,300, 58.4% margin). Emily Brown's Wedding has an estimated profit of $3,560 (54.9% margin on $6,480 revenue). [Demo Data]");
                response.setSuggestedActions(List.of("View Profitability Breakdown", "View Rental Workflow"));
                response.setRequiresApproval(false);
                break;

            case "WAREHOUSE_PREP":
                response.setMessage("Tomorrow's warehouse tasks include: 1 Pick List (250 Chiavari chairs & 25 round tables for Emily Brown Wedding), 1 Pack List (150 Gold Chiavari chairs for Jenkins Reception), and 1 Return Inspection (6 linen crates in Bay B-4).");
                response.setSuggestedActions(List.of("View Warehouse Tasks", "Print Pick Lists"));
                response.setRequiresApproval(false);
                break;

            case "DELIVERY_SCHEDULE":
                response.setMessage("4 deliveries are scheduled: 10:30 AM Evergreen Wedding Venue (Truck #4, David Wilson), 01:15 PM Dallas Arboretum (Truck #2, Mark Reynolds), 03:00 PM Fair Park Center (Truck #1, David Wilson), 05:30 PM Fort Worth Botanic Pickup (Truck #3).");
                response.setSuggestedActions(List.of("View Deliveries", "Driver Route Map"));
                response.setRequiresApproval(false);
                break;

            case "QUOTE_CALCULATION":
                response.setMessage("250 Chiavari Chairs at $8 each would be $2,000 before fees, discounts, and tax.");
                response.setSuggestedActions(List.of("Create Quote", "Send Quote to Customer"));
                response.setRequiresApproval(false);
                break;

            case "GENERAL_PRIORITIES":
            default:
                response.setMessage("Here are today's top priorities for Evergreen Event Rentals:\n1. Emily Brown Wedding quote (QUO-000001) in draft stage\n2. 4 deliveries scheduled today across Trucks #1, #2, #4\n3. Warehouse staging required for 250 chairs and 25 tables\n4. TechCorp Annual Gala contract pending ($12,500)");
                response.setSuggestedActions(List.of("View Rental Workflow", "View Deliveries", "View Warehouse"));
                response.setRequiresApproval(false);
                break;
        }

        return response;
    }
}
