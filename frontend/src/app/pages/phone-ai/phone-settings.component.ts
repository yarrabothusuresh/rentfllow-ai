import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { PhoneAiService } from './phone-ai.service';
import { PhoneTenantSettings } from './phone-ai.model';

@Component({
  selector: 'app-phone-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './phone-settings.component.html',
  styleUrls: ['./phone-settings.component.scss']
})
export class PhoneSettingsComponent implements OnInit {

  settings: PhoneTenantSettings = {
    phoneAiEnabled: true,
    provider: 'mock',
    inboundEnabled: true,
    recordingEnabled: false,
    transcriptionEnabled: true,
    transcriptRetentionDays: 90,
    humanHandoffNumber: '+1 (800) 555-0199',
    greeting: 'Thanks for calling ABC Event Rentals. I am the automated rental assistant.',
    disclosureText: 'This call may be recorded or transcribed to assist with your rental request.'
  };

  isLoading = true;
  isSaving = false;
  successMessage = '';

  constructor(private phoneService: PhoneAiService) {}

  ngOnInit(): void {
    this.loadSettings();
  }

  loadSettings(): void {
    this.isLoading = true;
    this.phoneService.getSettings().subscribe({
      next: (s) => {
        this.settings = s;
        this.isLoading = false;
      },
      error: (e) => {
        console.error('Failed to load settings', e);
        this.isLoading = false;
      }
    });
  }

  saveSettings(): void {
    this.isSaving = true;
    this.successMessage = '';
    this.phoneService.updateSettings(this.settings).subscribe({
      next: (res) => {
        this.settings = res;
        this.isSaving = false;
        this.successMessage = 'Voice AI telephony settings saved successfully!';
        setTimeout(() => this.successMessage = '', 4000);
      },
      error: (e) => {
        console.error('Failed to save settings', e);
        this.isSaving = false;
      }
    });
  }
}
