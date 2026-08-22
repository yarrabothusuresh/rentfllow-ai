import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../../../services/notification.service';
import { NotificationTemplateDTO } from '../../../models/notification.models';

@Component({
  selector: 'app-notification-templates',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './notification-templates.component.html',
  styleUrls: ['./notification-templates.component.scss']
})
export class NotificationTemplatesComponent implements OnInit {
  templates: NotificationTemplateDTO[] = [];
  loading = false;

  selectedTemplate: NotificationTemplateDTO | null = null;
  editModalOpen = false;
  previewData: { subject: string; body: string } | null = null;
  previewLoading = false;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.loadTemplates();
  }

  loadTemplates(): void {
    this.loading = true;
    this.notificationService.getTemplates().subscribe({
      next: (res: NotificationTemplateDTO[]) => {
        this.templates = res;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  editTemplate(t: NotificationTemplateDTO): void {
    this.selectedTemplate = { ...t };
    this.editModalOpen = true;
    this.renderLivePreview();
  }

  renderLivePreview(): void {
    if (!this.selectedTemplate || !this.selectedTemplate.id) return;
    this.previewLoading = true;
    this.notificationService.previewTemplate(this.selectedTemplate.id).subscribe({
      next: (res: { subject: string; body: string }) => {
        this.previewData = res;
        this.previewLoading = false;
      },
      error: () => this.previewLoading = false
    });
  }

  saveTemplate(): void {
    if (!this.selectedTemplate) return;
    if (this.selectedTemplate.id) {
      this.notificationService.updateTemplate(this.selectedTemplate.id, this.selectedTemplate).subscribe({
        next: () => {
          this.editModalOpen = false;
          this.loadTemplates();
        }
      });
    }
  }
}
