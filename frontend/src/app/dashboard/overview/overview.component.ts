import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { RoleStateService, RoleType } from '../../services/role-state.service';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss'
})
export class OverviewComponent implements OnInit {
  currentRole: RoleType = 'OWNER';

  constructor(
    private roleStateService: RoleStateService,
    private router: Router
  ) {}

  ngOnInit() {
    this.roleStateService.currentRole$.subscribe(role => {
      this.currentRole = role;
    });
  }

  investigateInsight(promptText: string) {
    this.router.navigate(['/dashboard/ai-copilot'], { queryParams: { prompt: promptText } });
  }

  demoAction(actionName: string) {
    alert(`[Demo] Action successful: ${actionName}`);
  }
}
