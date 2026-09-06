import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { PhoneAiService } from './phone-ai.service';
import { CallHandoffReason, CallStatus, PhoneCallSession, PhoneTranscriptSegment } from './phone-ai.model';

@Component({
  selector: 'app-phone-call-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './phone-call-detail.component.html',
  styleUrls: ['./phone-call-detail.component.scss']
})
export class PhoneCallDetailComponent implements OnInit {

  callId!: string;
  call: PhoneCallSession | null = null;
  transcripts: PhoneTranscriptSegment[] = [];
  isLoading = true;
  actionLoading = false;

  constructor(
    private route: ActivatedRoute,
    private phoneService: PhoneAiService
  ) {}

  ngOnInit(): void {
    this.callId = this.route.snapshot.paramMap.get('id') || '';
    if (this.callId) {
      this.loadCallDetail();
    }
  }

  loadCallDetail(): void {
    this.isLoading = true;
    this.phoneService.getCallDetail(this.callId).subscribe({
      next: (res) => {
        this.call = res.call;
        this.transcripts = res.transcripts || [];
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load call detail', err);
        this.isLoading = false;
      }
    });
  }

  transferToHuman(): void {
    if (!this.call) return;
    this.actionLoading = true;
    this.phoneService.requestHandoff(this.call.id, 'CUSTOMER_REQUEST').subscribe({
      next: () => {
        this.actionLoading = false;
        this.loadCallDetail();
      },
      error: (e) => {
        console.error('Failed handoff', e);
        this.actionLoading = false;
      }
    });
  }

  endCall(): void {
    if (!this.call) return;
    this.actionLoading = true;
    this.phoneService.endCall(this.call.id).subscribe({
      next: () => {
        this.actionLoading = false;
        this.loadCallDetail();
      },
      error: (e) => {
        console.error('Failed ending call', e);
        this.actionLoading = false;
      }
    });
  }

  getStatusBadgeClass(status: CallStatus): string {
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
