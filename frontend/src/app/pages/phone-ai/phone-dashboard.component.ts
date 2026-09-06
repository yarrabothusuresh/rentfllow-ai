import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { PhoneAiService } from './phone-ai.service';
import { CallStatus, PhoneCallSession, PhoneDashboardMetrics, PhoneTranscriptSegment } from './phone-ai.model';

@Component({
  selector: 'app-phone-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './phone-dashboard.component.html',
  styleUrls: ['./phone-dashboard.component.scss']
})
export class PhoneDashboardComponent implements OnInit {

  metrics: PhoneDashboardMetrics = {
    callsToday: 0,
    activeCalls: 0,
    waitingForHuman: 0,
    completedCalls: 0,
    failedCalls: 0,
    leadsCreated: 0
  };

  recentCalls: PhoneCallSession[] = [];
  isLoading = false;
  statusFilter: CallStatus | '' = '';

  // Simulator Drawer
  simulatorOpen = false;
  activeSimCall: PhoneCallSession | null = null;
  simCallerNumber = '+1 (555) 789-0123';
  simUtteranceInput = '';
  simTranscripts: PhoneTranscriptSegment[] = [];
  simLoading = false;

  constructor(private phoneService: PhoneAiService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.phoneService.getDashboard().subscribe({
      next: (m) => this.metrics = m,
      error: (e) => console.error('Failed to load phone metrics', e)
    });

    const statusParam = this.statusFilter ? this.statusFilter : undefined;
    this.phoneService.getCalls(statusParam, 0, 15).subscribe({
      next: (res) => {
        this.recentCalls = res.content || [];
        this.isLoading = false;
      },
      error: (e) => {
        console.error('Failed to load recent calls', e);
        this.isLoading = false;
      }
    });
  }

  onFilterChange(status: CallStatus | ''): void {
    this.statusFilter = status;
    this.loadData();
  }

  // --- Live Call Simulator ---
  openSimulator(): void {
    this.simulatorOpen = true;
    this.activeSimCall = null;
    this.simTranscripts = [];
  }

  closeSimulator(): void {
    if (this.activeSimCall && this.activeSimCall.status !== 'COMPLETED') {
      this.phoneService.endCall(this.activeSimCall.id).subscribe();
    }
    this.simulatorOpen = false;
    this.activeSimCall = null;
    this.loadData();
  }

  startSimulatedCall(): void {
    this.simLoading = true;
    this.phoneService.simulateInboundCall(this.simCallerNumber).subscribe({
      next: (call) => {
        this.activeSimCall = call;
        this.simLoading = false;
        this.loadSimTranscripts(call.id);
        this.loadData();
      },
      error: (err) => {
        console.error('Failed to start call', err);
        this.simLoading = false;
      }
    });
  }

  sendSimUtterance(): void {
    if (!this.activeSimCall || !this.simUtteranceInput.trim()) return;

    const utterance = this.simUtteranceInput.trim();
    this.simUtteranceInput = '';
    this.simLoading = true;

    this.phoneService.sendUtterance(this.activeSimCall.id, utterance).subscribe({
      next: (res) => {
        this.simLoading = false;
        if (this.activeSimCall) {
          this.activeSimCall.status = res.status;
          if (res.leadId) this.activeSimCall.leadId = res.leadId;
        }
        this.loadSimTranscripts(this.activeSimCall!.id);
      },
      error: (err) => {
        console.error('Failed to send utterance', err);
        this.simLoading = false;
      }
    });
  }

  triggerSimHandoff(): void {
    if (!this.activeSimCall) return;
    this.simLoading = true;
    this.phoneService.requestHandoff(this.activeSimCall.id, 'CUSTOMER_REQUEST').subscribe({
      next: () => {
        this.simLoading = false;
        if (this.activeSimCall) this.activeSimCall.status = 'WAITING_FOR_HUMAN';
        this.loadSimTranscripts(this.activeSimCall!.id);
        this.loadData();
      },
      error: (e) => {
        console.error('Failed handoff', e);
        this.simLoading = false;
      }
    });
  }

  endSimCall(): void {
    if (!this.activeSimCall) return;
    this.phoneService.endCall(this.activeSimCall.id).subscribe({
      next: () => {
        if (this.activeSimCall) this.activeSimCall.status = 'COMPLETED';
        this.loadSimTranscripts(this.activeSimCall!.id);
        this.loadData();
      }
    });
  }

  private loadSimTranscripts(callId: string): void {
    this.phoneService.getCallDetail(callId).subscribe({
      next: (res) => {
        this.simTranscripts = res.transcripts || [];
      }
    });
  }

  getStatusClass(status: CallStatus): string {
    switch (status) {
      case 'AI_ACTIVE': return 'badge-active';
      case 'WAITING_FOR_HUMAN': return 'badge-warning';
      case 'HUMAN_ACTIVE': return 'badge-info';
      case 'COMPLETED': return 'badge-success';
      case 'FAILED': return 'badge-danger';
      default: return 'badge-neutral';
    }
  }
}
