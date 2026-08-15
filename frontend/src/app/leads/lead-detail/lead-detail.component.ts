import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CrmService } from '../../services/crm.service';
import { Lead, LeadConversionResult, Customer } from '../../models/crm.models';

@Component({
  selector: 'app-lead-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './lead-detail.component.html',
  styleUrl: './lead-detail.component.scss'
})
export class LeadDetailComponent implements OnInit {
  lead: Lead | null = null;
  loading = true;
  error: string | null = null;

  // Conversion Modal State
  showConvertModal = false;
  conversionResult: LeadConversionResult | null = null;
  conversionLoading = false;
  possibleDuplicate: Customer | null = null;

  // Timeline stages
  timelineStages = [
    { key: 'NEW', label: 'Lead Created' },
    { key: 'CONTACTED', label: 'Contacted' },
    { key: 'QUALIFIED', label: 'Qualified' },
    { key: 'QUOTE_REQUESTED', label: 'Quote Requested' },
    { key: 'QUOTE_SENT', label: 'Quote Sent' },
    { key: 'NEGOTIATION', label: 'Negotiation' },
    { key: 'CONVERTED', label: 'Converted' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private crmService: CrmService
  ) {}

  ngOnInit(): void {
    const leadId = this.route.snapshot.paramMap.get('id');
    if (leadId) {
      this.loadLead(leadId);
    } else {
      this.error = 'Invalid lead ID';
      this.loading = false;
    }
  }

  loadLead(id: string): void {
    this.loading = true;
    this.crmService.getLeadById(id).subscribe({
      next: (data) => {
        this.lead = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Lead could not be found.';
        this.loading = false;
      }
    });
  }

  openConvertModal(): void {
    this.showConvertModal = true;
    this.conversionResult = null;
    this.possibleDuplicate = null;
  }

  closeConvertModal(): void {
    this.showConvertModal = false;
  }

  executeConversion(forceNew = false, useExistingId?: string): void {
    if (!this.lead || !this.lead.id) return;

    this.conversionLoading = true;
    this.crmService.convertLead(this.lead.id, {
      forceNewCustomer: forceNew,
      useExistingCustomerId: useExistingId
    }).subscribe({
      next: (res) => {
        this.conversionLoading = false;
        this.conversionResult = res;

        if (res.possibleDuplicateFound && res.duplicateCustomer) {
          this.possibleDuplicate = res.duplicateCustomer;
        } else if (res.status === 'SUCCESS') {
          this.possibleDuplicate = null;
          // Refresh lead status
          this.loadLead(this.lead!.id!);
        }
      },
      error: (err) => {
        this.conversionLoading = false;
        alert(err.message || 'Conversion failed.');
      }
    });
  }

  isStageCompleted(stageKey: string): boolean {
    if (!this.lead || !this.lead.status) return false;

    const stagesOrder = ['NEW', 'CONTACTED', 'QUALIFIED', 'QUOTE_REQUESTED', 'QUOTE_SENT', 'NEGOTIATION', 'CONVERTED'];
    const currentIndex = stagesOrder.indexOf(this.lead.status);
    const targetIndex = stagesOrder.indexOf(stageKey);

    if (this.lead.status === 'LOST') return false;
    return targetIndex <= currentIndex;
  }

  isCurrentStage(stageKey: string): boolean {
    return this.lead?.status === stageKey;
  }
}
