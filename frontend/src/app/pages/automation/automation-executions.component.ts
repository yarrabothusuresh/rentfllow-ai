import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AutomationService } from './automation.service';
import { AutomationExecution } from './automation.model';

@Component({
  selector: 'app-automation-executions',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './automation-executions.component.html',
  styleUrls: ['./automation-executions.component.scss']
})
export class AutomationExecutionsComponent implements OnInit {
  executions: AutomationExecution[] = [];
  loading: boolean = false;
  selectedRole: string = 'ADMIN';

  constructor(private automationService: AutomationService) {}

  ngOnInit(): void {
    this.loadExecutions();
  }

  loadExecutions(): void {
    this.loading = true;
    this.automationService.getExecutions(0, 50, this.selectedRole).subscribe({
      next: (data) => {
        this.executions = data.content || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load executions', err);
        this.loading = false;
      }
    });
  }
}
