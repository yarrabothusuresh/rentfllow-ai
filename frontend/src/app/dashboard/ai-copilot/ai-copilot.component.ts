import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

interface ChatMessage {
  role: 'user' | 'ai';
  content: string;
  actions?: string[];
}

@Component({
  selector: 'app-ai-copilot',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ai-copilot.component.html',
  styleUrl: './ai-copilot.component.scss'
})
export class AiCopilotComponent {
  messages: ChatMessage[] = [];
  
  suggestedQuestions = [
    "Show me the status of Emily Brown's wedding",
    "Show me today's rental workflow",
    "Which bookings are waiting for customer action?",
    "Which events are at risk?",
    "What should the warehouse prepare today?",
    "Which deliveries are scheduled today?",
    "Is Evergreen Event Rentals a good fit for RentFlow AI?",
    "Show me today's priorities"
  ];

  constructor(private router: Router) {}

  askQuestion(question: string) {
    if (!question || question.trim() === '') return;
    
    // Add user message
    this.messages.push({ role: 'user', content: question });
    
    // Simulate AI delay and response
    setTimeout(() => {
      this.generateMockResponse(question);
    }, 800);
  }

  generateMockResponse(question: string) {
    const qLower = question.toLowerCase();

    if (qLower.includes("emily brown") || (qLower.includes("status") && qLower.includes("wedding"))) {
      this.messages.push({
        role: 'ai',
        content: `
          <p><strong>Emily Brown's Wedding Status (Dallas, TX — Sept 20, 2026):</strong></p>
          <ul style="list-style: none; padding-left: 0; line-height: 1.6;">
            <li>✓ Quote sent ($6,480 total)</li>
            <li>✓ Inventory available (250 Chiavari chairs & 25 tables)</li>
            <li>○ Booking awaiting customer confirmation & deposit</li>
            <li>○ Warehouse preparation pending</li>
            <li>○ Delivery route not assigned</li>
          </ul>
          <p style="margin-top: 10px;"><strong>Recommended Next Action:</strong><br>Follow up with the customer about the quote.</p>
        `,
        actions: ['View Rental Workflow', 'Send Follow-up Email']
      });
    } else if (qLower.includes("workflow") || qLower.includes("today's rental")) {
      this.messages.push({
        role: 'ai',
        content: `
          <p>Here is today's rental workflow overview across 12 lifecycle stages:</p>
          <p>• <strong>12 Active Events</strong> in pipeline<br>
             • <strong>4 Quotes Awaiting Customer Action</strong><br>
             • <strong>3 Bookings Preparing</strong> in warehouse<br>
             • <strong>5 Deliveries Scheduled</strong> for today</p>
        `,
        actions: ['View Rental Workflow']
      });
    } else if (qLower.includes("customer action") || qLower.includes("waiting")) {
      this.messages.push({
        role: 'ai',
        content: `
          <p>Found 4 bookings waiting for customer action:</p>
          <ol>
            <li><strong>Emily Brown (Wedding):</strong> Awaiting quote acceptance ($6,480).</li>
            <li><strong>TechCorp (Gala):</strong> Awaiting signed contract.</li>
            <li><strong>Sarah Jenkins (Reception):</strong> Awaiting 25% deposit payment.</li>
            <li><strong>Austin Festival:</strong> Awaiting venue access detail confirmation.</li>
          </ol>
        `,
        actions: ['View Rental Workflow', 'Send Reminders']
      });
    } else if (qLower.includes("at risk")) {
      this.messages.push({
        role: 'ai',
        content: `
          <p>⚠️ <strong>2 Events At Risk:</strong></p>
          <p>1. <strong>Jenkins Wedding:</strong> Short 30 white folding chairs for Saturday.<br>
             2. <strong>Dallas Arboretum Gala:</strong> Truck #3 route conflict at 10:00 AM.</p>
        `,
        actions: ['View Rental Workflow', 'Resolve Shortage']
      });
    } else if (qLower.includes("warehouse prepare")) {
      this.messages.push({
        role: 'ai',
        content: `
          <p><strong>Warehouse Staging for Today:</strong></p>
          <p>• Pack 250 Chiavari chairs & 25 round tables for Emily Brown Wedding.<br>
             • Pick 150 gold Chiavari chairs for Evergreen Reception.<br>
             • Inspect 6 returned linen crates in receiving bay B-4.</p>
        `,
        actions: ['View Rental Workflow', 'Print Pick Lists']
      });
    } else if (qLower.includes("deliveries")) {
      this.messages.push({
        role: 'ai',
        content: `
          <p><strong>Today's Delivery Schedule (5 Jobs):</strong></p>
          <p>1. 10:30 AM — Evergreen Wedding Venue (Stop 1, Truck 4)<br>
             2. 01:15 PM — Dallas Arboretum Pavilion (Stop 2, Truck 2)<br>
             3. 03:00 PM — Fair Park Center (Stop 3, Truck 1)</p>
        `,
        actions: ['View Rental Workflow', 'Driver Route Map']
      });
    } else if (qLower.includes("evergreen") && qLower.includes("good fit")) {
      this.messages.push({
        role: 'ai',
        content: `
          <p>Yes. Based on the current demo profile, Evergreen Event Rentals matches our initial ICP because it operates a sizable rental catalog, manages recurring events and has multiple operational workflows that could benefit from centralized rental management and AI-assisted sales.</p>
        `,
        actions: ['View ICP']
      });
    } else if (qLower.includes("priorities")) {
      this.messages.push({
        role: 'ai',
        content: `
          <p>Good morning. I found 5 priorities:</p>
          <ol>
            <li>ABC Weddings owes $4,200 and the event is tomorrow.</li>
            <li>You're short 30 white chairs for Saturday.</li>
            <li>Event #1048 still needs a signed contract.</li>
            <li>Truck #3 has a delivery conflict.</li>
            <li>The warehouse team needs today's pickup list.</li>
          </ol>
        `,
        actions: ['Send Payment Reminder', 'Find Available Chairs', 'Review Contract', 'Resolve Delivery', 'Create Pick List']
      });
    } else {
      this.messages.push({
        role: 'ai',
        content: `<p>I have analyzed your request regarding "${question}". This feature is currently in demo mode.</p>`,
        actions: ['View Rental Workflow', 'Return to Dashboard']
      });
    }
  }

  demoAction(actionName: string) {
    if (actionName === 'View ICP') {
      this.router.navigate(['/ideal-customer']);
    } else if (actionName === 'View Rental Workflow') {
      this.router.navigate(['/workflow-demo']);
    } else {
      alert(`[Demo] Action successful: ${actionName}`);
    }
  }
}

