import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AiSalesService } from '../../services/ai-sales.service';
import { AiChatResponse, AiSalesMessage } from '../../models/ai-sales.model';

@Component({
  selector: 'app-customer-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="chat-widget-wrapper">
      <div class="chat-card">
        <!-- Chat Header -->
        <div class="chat-header">
          <div class="header-info">
            <div class="agent-avatar">
              <i class="bi bi-robot"></i>
            </div>
            <div>
              <h3>RentFlow Rental Assistant</h3>
              <span class="online-indicator">
                <span class="dot"></span> Online &bull; Instant Equipment Discovery
              </span>
            </div>
          </div>
        </div>

        <!-- Chat Stream -->
        <div class="chat-messages" #messagesContainer>
          <div
            *ngFor="let msg of messages"
            class="message-row"
            [ngClass]="msg.senderType === 'CUSTOMER' ? 'msg-customer' : 'msg-ai'"
          >
            <div class="avatar" *ngIf="msg.senderType !== 'CUSTOMER'">
              <i class="bi bi-robot"></i>
            </div>
            <div class="bubble">
              <div class="bubble-text">{{ msg.content }}</div>
              <span class="bubble-time">{{ msg.createdAt | date:'shortTime' }}</span>
            </div>
          </div>

          <!-- Thinking animation -->
          <div *ngIf="isThinking" class="message-row msg-ai">
            <div class="avatar"><i class="bi bi-robot"></i></div>
            <div class="bubble thinking-bubble">
              <span class="dot-pulse"></span>
              <span class="dot-pulse"></span>
              <span class="dot-pulse"></span>
              <em>Checking rental availability & calculating pricing...</em>
            </div>
          </div>
        </div>

        <!-- Suggested Quick Replies -->
        <div class="suggested-chips" *ngIf="suggestedReplies.length > 0 && !isThinking">
          <button
            *ngFor="let suggestion of suggestedReplies"
            (click)="selectSuggestedReply(suggestion)"
            class="chip-btn"
          >
            {{ suggestion }}
          </button>
        </div>

        <!-- Input Box -->
        <div class="chat-input-bar">
          <input
            type="text"
            [(ngModel)]="userInput"
            (keyup.enter)="send()"
            placeholder="Ask about tables, chairs, event dates, or quote requests..."
            [disabled]="isThinking"
          />
          <button
            (click)="send()"
            class="btn-send"
            [disabled]="!userInput.trim() || isThinking"
          >
            <i class="bi bi-send-fill"></i>
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .chat-widget-wrapper {
      max-width: 650px;
      margin: 0 auto;
      height: 700px;
      display: flex;
      flex-direction: column;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }

    .chat-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      display: flex;
      flex-direction: column;
      height: 100%;
      overflow: hidden;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
    }

    .chat-header {
      background: #0f172a;
      color: white;
      padding: 1rem 1.25rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .header-info { display: flex; align-items: center; gap: 0.75rem; }
    .agent-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: #2563eb;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem;
    }

    .chat-header h3 { margin: 0; font-size: 1.05rem; font-weight: 600; }
    .online-indicator {
      font-size: 0.75rem;
      color: #94a3b8;
      display: flex;
      align-items: center;
      gap: 0.35rem;
      margin-top: 0.15rem;
    }
    .online-indicator .dot {
      width: 7px;
      height: 7px;
      background: #22c55e;
      border-radius: 50%;
    }

    .chat-messages {
      flex: 1;
      overflow-y: auto;
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      gap: 1rem;
      background: #f8fafc;
    }

    .message-row { display: flex; gap: 0.6rem; align-items: flex-end; max-width: 82%; }
    .msg-ai { align-self: flex-start; }
    .msg-customer { align-self: flex-end; flex-direction: row-reverse; }

    .avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: #e2e8f0;
      color: #3b82f6;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.9rem;
      flex-shrink: 0;
    }

    .bubble {
      padding: 0.75rem 1rem;
      border-radius: 12px;
      box-shadow: 0 1px 2px rgba(0,0,0,0.04);
      position: relative;
    }
    .msg-ai .bubble {
      background: white;
      border: 1px solid #e2e8f0;
      color: #1e293b;
      border-bottom-left-radius: 4px;
    }
    .msg-customer .bubble {
      background: #3b82f6;
      color: white;
      border-bottom-right-radius: 4px;
    }

    .bubble-text { font-size: 0.875rem; line-height: 1.5; white-space: pre-line; }
    .bubble-time { font-size: 0.7rem; color: #94a3b8; margin-top: 0.35rem; display: block; text-align: right; }
    .msg-customer .bubble-time { color: #dbeafe; }

    .thinking-bubble { display: flex; align-items: center; gap: 0.4rem; color: #059669; font-size: 0.825rem; }
    .dot-pulse {
      width: 5px;
      height: 5px;
      background: #059669;
      border-radius: 50%;
      animation: pulse 1.2s infinite ease-in-out both;
    }
    @keyframes pulse { 0%, 80%, 100% { opacity: 0.3; } 40% { opacity: 1; } }

    .suggested-chips {
      display: flex;
      gap: 0.5rem;
      padding: 0.6rem 1rem;
      background: #f1f5f9;
      overflow-x: auto;
      border-top: 1px solid #e2e8f0;
    }
    .chip-btn {
      background: white;
      border: 1px solid #cbd5e1;
      padding: 0.35rem 0.75rem;
      border-radius: 9999px;
      font-size: 0.8rem;
      font-weight: 500;
      color: #334155;
      cursor: pointer;
      white-space: nowrap;
      transition: all 0.15s ease-in-out;
    }
    .chip-btn:hover {
      background: #eff6ff;
      border-color: #93c5fd;
      color: #1e40af;
    }

    .chat-input-bar {
      display: flex;
      padding: 0.85rem 1rem;
      background: white;
      border-top: 1px solid #e2e8f0;
      gap: 0.5rem;
    }
    .chat-input-bar input {
      flex: 1;
      padding: 0.65rem 0.85rem;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-size: 0.875rem;
      outline: none;
    }
    .chat-input-bar input:focus { border-color: #3b82f6; }

    .btn-send {
      background: #3b82f6;
      color: white;
      border: none;
      border-radius: 8px;
      width: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
    }
    .btn-send:hover { background: #2563eb; }
    .btn-send:disabled { background: #cbd5e1; cursor: not-allowed; }
  `]
})
export class CustomerChatWidgetComponent implements OnInit {
  conversationId: string | null = null;
  messages: AiSalesMessage[] = [];
  suggestedReplies: string[] = [];
  userInput = '';
  isThinking = false;

  constructor(private aiSalesService: AiSalesService) {}

  ngOnInit(): void {
    this.initConversation();
  }

  initConversation(): void {
    this.isThinking = true;
    this.aiSalesService.startCustomerConversation({
      message: 'Hello! I need help planning event rentals.'
    }).subscribe({
      next: (res) => {
        this.conversationId = res.conversationId;
        this.isThinking = false;
        this.messages.push({
          senderType: 'AI',
          content: res.replyText,
          createdAt: new Date().toISOString()
        });
        this.suggestedReplies = res.suggestedReplies || [];
      },
      error: (err) => {
        console.error('Failed to initialize customer chat', err);
        this.isThinking = false;
      }
    });
  }

  send(): void {
    if (!this.userInput.trim() || !this.conversationId) return;
    const text = this.userInput.trim();
    this.userInput = '';

    this.messages.push({
      senderType: 'CUSTOMER',
      content: text,
      createdAt: new Date().toISOString()
    });

    this.isThinking = true;
    this.suggestedReplies = [];

    this.aiSalesService.sendCustomerMessage(this.conversationId, {
      message: text
    }).subscribe({
      next: (res) => {
        this.isThinking = false;
        this.messages.push({
          senderType: 'AI',
          content: res.replyText,
          createdAt: new Date().toISOString()
        });
        this.suggestedReplies = res.suggestedReplies || [];
      },
      error: (err) => {
        console.error('Failed to send message', err);
        this.isThinking = false;
        this.messages.push({
          senderType: 'AI',
          content: 'Sorry, I am having trouble answering right now. Our sales staff has been notified.',
          createdAt: new Date().toISOString()
        });
      }
    });
  }

  selectSuggestedReply(reply: string): void {
    this.userInput = reply;
    this.send();
  }
}
