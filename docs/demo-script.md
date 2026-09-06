# RentFlow AI — Master Demonstration Script (15–20 Minutes)

This script provides a step-by-step walkthrough for demonstrating the complete RentFlow AI platform to stakeholders, investors, or enterprise rental clients.

---

## Act 1: The Customer Discovery & AI Sales Agent (Minutes 0–5)
1. **Storefront Browsing**:
   - Open browser to `http://localhost:4200/storefront`.
   - Browse catalog (Tents, Chairs, Lighting, Audio). Filter by category and date range.
2. **AI Sales Agent Chat**:
   - Click the AI floating widget in the lower-right corner.
   - Enter: *"Hi! I'm planning a wedding in Austin on October 15th for 150 guests. What do you recommend?"*
   - Observe real-time response: AI checks real inventory availability and returns suggested Gold Chiavari Chairs, 40x60 Pole Tent, and bistro lighting.
   - Enter: *"Can you add 100 gold chiavari chairs to my rental request?"*
   - Observe lead creation and automatic generation of draft quote.

---

## Act 2: Internal Sales & Contract Execution (Minutes 5–9)
1. **CRM & Lead Pipeline**:
   - Switch to internal dashboard at `http://localhost:4200/dashboard/crm`.
   - View newly created lead for "Sarah Jenkins".
   - Review AI engagement notes and estimated quote value.
2. **Draft Quote Review & Conversion**:
   - Navigate to Quotes (`/dashboard/quotes`).
   - Review draft quote calculations (subtotal, delivery, taxes, 30% deposit).
   - Click **Send Quote to Customer**.
3. **Customer Portal & E-Signature**:
   - Open Customer Portal view (`/portal/quotes`).
   - Customer accepts quote, reviews rental agreement terms, and draws e-signature.
   - Customer pays initial 30% deposit ($500.00) via secure Stripe/Credit Card form.
   - Booking automatically changes to `CONFIRMED`, and inventory units are hard-reserved.

---

## Act 3: Operations, Warehouse & Dispatch (Minutes 9–13)
1. **Warehouse Pick & Pack**:
   - Navigate to Warehouse Operations (`/dashboard/warehouse`).
   - Open the warehouse fulfillment order.
   - Show barcode/serial scanning: Pick 100 items, verify quantities, mark order `PACKED`.
2. **Dispatch & Delivery Route**:
   - Navigate to Deliveries (`/dashboard/deliveries`).
   - Assign driver and vehicle. Mark route `DISPATCHED` -> `DELIVERED`.
3. **Internal Copilot Inquiry**:
   - Open internal AI Copilot modal (`Ctrl+K` or top search bar).
   - Ask: *"Which deliveries are scheduled for today?"*
   - Ask: *"What is our gross margin on the Jenkins wedding booking?"*
   - Notice explainable analytical breakdown citing exact database entities.

---

## Act 4: Event Return, Inspection & Claims (Minutes 13–16)
1. **Return Order Check-in**:
   - Event concludes. Navigate to Returns (`/dashboard/returns`).
   - Perform itemized check-in: 90 units returned in good condition, 5 damaged, 5 missing.
2. **Damage Claim & Estimate**:
   - Navigate to Damage Claims (`/dashboard/claims`).
   - Review automatically generated claim with photos and repair estimate ($175.00).
3. **Final Invoicing & Payment**:
   - Navigate to Invoices (`/dashboard/invoices`).
   - Final invoice generated: Remaining rental balance ($526.00) + damage repairs ($175.00) = $701.00.
   - Mark final payment completed. Booking transitions to `COMPLETED`.

---

## Act 5: Phone AI Foundation & Proactive Automation (Minutes 16–20)
1. **Phone AI Console**:
   - Navigate to `/dashboard/phone-ai`.
   - Show active call sessions, consent status, and call transcripts.
   - Click **Simulate Inbound Call**: Enter sample inquiry *"Do you have white folding chairs available next Saturday?"*
   - Listen to simulated voice response, observe transcript segments live.
   - Show security guardrail: Ask *"Can I give you my credit card?"* -> Voice AI politely refuses and offers secure SMS link.
2. **AI Recommendations & Automation**:
   - Navigate to `/dashboard/automation`.
   - Show proactive signals (low-margin warnings, expiring quotes, maintenance alerts).
   - Review AI explanation and click **Approve & Execute** with zero hallucination.
