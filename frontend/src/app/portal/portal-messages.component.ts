import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerPortalService, CustomerConversation } from '../services/customer-portal.service';

@Component({
  selector: 'app-portal-messages',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="messages-container p-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 class="text-light mb-1">Customer Messaging Center</h2>
          <p class="text-muted mb-0">Direct communication with your rental account manager</p>
        </div>
        <button class="btn btn-info" (click)="showNewDialog = true">+ New Question</button>
      </div>

      <div class="row g-4">
        <!-- Conversation List Sidebar -->
        <div class="col-md-4">
          <div class="card-glass p-3 h-100">
            <h5 class="text-light mb-3">Conversations</h5>
            <div *ngIf="conversations.length === 0" class="text-muted small">No conversations started yet.</div>

            <div class="list-group list-group-flush bg-transparent">
              <button *ngFor="let conv of conversations" 
                      class="list-group-item list-group-item-action bg-transparent text-light border-secondary p-3 mb-2 rounded border"
                      [class.active-conv]="selectedConv?.id === conv.id"
                      (click)="selectConversation(conv)">
                <div class="d-flex justify-content-between align-items-center mb-1">
                  <strong class="text-truncate">{{ conv.subject }}</strong>
                  <span class="badge bg-secondary font-monospace" style="font-size: 0.65rem;">{{ conv.status }}</span>
                </div>
                <small class="text-muted d-block">{{ conv.bookingNumber ? 'Ref: ' + conv.bookingNumber : 'General Question' }}</small>
                <small class="text-muted">{{ conv.updatedAt | date:'short' }}</small>
              </button>
            </div>
          </div>
        </div>

        <!-- Selected Thread Detail -->
        <div class="col-md-8">
          <div class="card-glass p-4 h-100 d-flex flex-direction-column" *ngIf="selectedConv">
            <div class="thread-header border-bottom border-secondary pb-3 mb-3 d-flex justify-content-between align-items-center">
              <div>
                <h4 class="text-light mb-0">{{ selectedConv.subject }}</h4>
                <small class="text-muted">Status: {{ selectedConv.status }}</small>
              </div>
            </div>

            <!-- Messages Stream -->
            <div class="messages-stream flex-grow-1 overflow-auto mb-3 pe-2" style="max-height: 380px;">
              <div *ngFor="let msg of selectedConv.messages" 
                   class="msg-bubble p-3 rounded mb-3 max-w-75"
                   [class.msg-customer]="msg.senderType === 'CUSTOMER'"
                   [class.msg-staff]="msg.senderType !== 'CUSTOMER'">
                <div class="msg-header small text-muted mb-1">
                  <strong>{{ msg.senderType === 'CUSTOMER' ? 'You' : 'Rental Staff' }}</strong> • {{ msg.createdAt | date:'shortTime' }}
                </div>
                <div class="msg-text text-light">{{ msg.message }}</div>
              </div>
            </div>

            <!-- Reply Box -->
            <div class="reply-box pt-3 border-top border-secondary">
              <div class="input-group">
                <textarea [(ngModel)]="replyText" placeholder="Type your reply to staff..." class="form-control bg-dark text-light border-secondary" rows="2"></textarea>
                <button class="btn btn-info px-4" (click)="sendReply()">Send</button>
              </div>
            </div>
          </div>

          <div class="card-glass p-5 text-center text-muted h-100 d-flex align-items-center justify-content-center" *ngIf="!selectedConv">
            <p class="mb-0">Select a conversation from the left to view messages</p>
          </div>
        </div>
      </div>

      <!-- New Message Modal -->
      <div class="modal-backdrop-custom" *ngIf="showNewDialog">
        <div class="card-glass modal-box p-4">
          <h4 class="text-light mb-3">Ask a Question</h4>
          <div class="mb-3">
            <label class="text-muted small">Subject</label>
            <input type="text" [(ngModel)]="newSubject" placeholder="Question about delivery timing..." class="form-control bg-dark text-light border-secondary" />
          </div>
          <div class="mb-3">
            <label class="text-muted small">Message</label>
            <textarea [(ngModel)]="newMessage" rows="4" placeholder="Enter message for rental management..." class="form-control bg-dark text-light border-secondary"></textarea>
          </div>
          <div class="d-flex justify-content-end gap-2">
            <button class="btn btn-outline-secondary" (click)="showNewDialog = false">Cancel</button>
            <button class="btn btn-info" (click)="createNewConversation()">Send Question</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .messages-container { color: #f8fafc; }
    .card-glass {
      background: rgba(30, 41, 59, 0.7);
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
    }
    .active-conv {
      border-color: #38bdf8 !important;
      background: rgba(56, 189, 248, 0.1) !important;
    }
    .msg-bubble {
      max-width: 80%;
      border-radius: 12px;
    }
    .msg-customer {
      margin-left: auto;
      background: #0284c7;
    }
    .msg-staff {
      margin-right: auto;
      background: #334155;
    }
    .modal-backdrop-custom {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0, 0, 0, 0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1050;
    }
    .modal-box {
      width: 500px;
    }
  `]
})
export class PortalMessagesComponent implements OnInit {
  conversations: CustomerConversation[] = [];
  selectedConv: CustomerConversation | null = null;

  replyText: string = '';

  showNewDialog: boolean = false;
  newSubject: string = '';
  newMessage: string = '';

  constructor(private portalService: CustomerPortalService) {}

  ngOnInit(): void {
    this.loadConversations();
  }

  loadConversations(): void {
    this.portalService.getMessages().subscribe({
      next: (data) => {
        this.conversations = data;
        if (this.conversations.length > 0 && !this.selectedConv) {
          this.selectedConv = this.conversations[0];
        }
      },
      error: () => {
        // Fallback demo data
        this.conversations = [{
          id: 'conv-1',
          customerId: 'cust-1',
          bookingNumber: 'BOOK-000123',
          subject: 'Question about BOOK-000123 Delivery Time',
          status: 'WAITING_FOR_CUSTOMER',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          messages: [
            { id: 'm1', senderType: 'CUSTOMER', message: 'Hi, can we adjust delivery time to 9 AM?', createdAt: new Date(Date.now() - 3600000).toISOString() },
            { id: 'm2', senderType: 'STAFF', message: 'Sure Emily! 9 AM delivery slot has been confirmed for Aug 31.', createdAt: new Date().toISOString() }
          ]
        }];
        this.selectedConv = this.conversations[0];
      }
    });
  }

  selectConversation(conv: CustomerConversation): void {
    this.selectedConv = conv;
  }

  sendReply(): void {
    if (!this.selectedConv || !this.replyText) return;
    this.portalService.sendMessage({
      conversationId: this.selectedConv.id,
      message: this.replyText
    }).subscribe({
      next: () => {
        this.replyText = '';
        this.loadConversations();
      }
    });
  }

  createNewConversation(): void {
    if (!this.newSubject || !this.newMessage) return;
    this.portalService.sendMessage({
      subject: this.newSubject,
      message: this.newMessage
    }).subscribe({
      next: () => {
        this.showNewDialog = false;
        this.newSubject = '';
        this.newMessage = '';
        this.loadConversations();
      }
    });
  }
}
