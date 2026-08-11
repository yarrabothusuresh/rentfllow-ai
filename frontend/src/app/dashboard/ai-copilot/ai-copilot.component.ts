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
    "Is Evergreen Event Rentals a good fit for RentFlow AI?",
    "Show me today's priorities",
    "Which customers haven't paid?",
    "Do I have enough chairs for Saturday?",
    "Which events are most profitable?",
    "Which products are underutilized?",
    "Create a quote for a 250-person wedding"
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
    if (qLower.includes("evergreen") && qLower.includes("good fit")) {
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
        actions: ['View Analytics', 'Return to Dashboard']
      });
    }
  }

  demoAction(actionName: string) {
    if (actionName === 'View ICP') {
      this.router.navigate(['/ideal-customer']);
    } else {
      alert(`[Demo] Action successful: ${actionName}`);
    }
  }
}

