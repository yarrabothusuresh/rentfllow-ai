import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { WorkflowService, WorkflowData, WorkflowStageItem, ViewMode } from '../services/workflow.service';
import { RoleStateService, RoleType } from '../services/role-state.service';

@Component({
  selector: 'app-workflow-demo',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './workflow-demo.component.html',
  styleUrl: './workflow-demo.component.scss'
})
export class WorkflowDemoComponent implements OnInit, OnDestroy {
  workflowData: WorkflowData | null = null;
  selectedStage: WorkflowStageItem | null = null;
  viewMode: ViewMode = 'INTERNAL';
  currentRole: RoleType = 'OWNER';
  actionToastMessage: string | null = null;

  private subs: Subscription[] = [];

  constructor(
    public workflowService: WorkflowService,
    public roleStateService: RoleStateService
  ) {}

  ngOnInit(): void {
    this.subs.push(
      this.workflowService.workflowData$.subscribe(data => {
        this.workflowData = data;
        if (data && data.stages) {
          // If no stage is selected yet, select the CURRENT stage
          if (!this.selectedStage) {
            this.selectedStage = data.stages.find(s => s.status === 'CURRENT') || data.stages[0];
          } else {
            // Re-sync selected stage with new data
            const updated = data.stages.find(s => s.stageKey === this.selectedStage?.stageKey);
            if (updated) this.selectedStage = updated;
          }
        }
      })
    );

    this.subs.push(
      this.workflowService.viewMode$.subscribe(mode => {
        this.viewMode = mode;
      })
    );

    this.subs.push(
      this.roleStateService.currentRole$.subscribe(role => {
        this.currentRole = role;
        if (role === 'CUSTOMER') {
          this.workflowService.setViewMode('CUSTOMER');
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  advanceWorkflow(): void {
    this.workflowService.advanceWorkflow();
    this.showToast('Workflow advanced to next operational stage!');
  }

  resetWorkflow(): void {
    this.workflowService.resetWorkflow();
    this.showToast('Workflow reset to Quote stage.');
  }

  selectStage(stage: WorkflowStageItem): void {
    this.selectedStage = stage;
  }

  toggleViewMode(mode: ViewMode): void {
    this.workflowService.setViewMode(mode);
  }

  isStageRelevantToRole(stage: WorkflowStageItem): boolean {
    if (this.currentRole === 'OWNER' || this.currentRole === 'ADMIN') return true;

    switch (this.currentRole) {
      case 'SALES':
        return ['INQUIRY', 'LEAD', 'QUOTE', 'BOOKING'].includes(stage.stageKey);
      case 'WAREHOUSE':
        return ['INVENTORY', 'WAREHOUSE', 'RETURN'].includes(stage.stageKey);
      case 'DRIVER':
        return ['DELIVERY', 'PICKUP'].includes(stage.stageKey);
      case 'CUSTOMER':
        return ['QUOTE', 'BOOKING', 'PAYMENT', 'DELIVERY', 'EVENT'].includes(stage.stageKey);
      default:
        return true;
    }
  }

  triggerStageAction(actionName: string): void {
    this.showToast(`Action executed: ${actionName}`);
  }

  private showToast(msg: string): void {
    this.actionToastMessage = msg;
    setTimeout(() => {
      if (this.actionToastMessage === msg) {
        this.actionToastMessage = null;
      }
    }, 3000);
  }
}
