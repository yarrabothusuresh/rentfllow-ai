import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
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

  constructor(private roleStateService: RoleStateService) {}

  ngOnInit() {
    this.roleStateService.currentRole$.subscribe(role => {
      this.currentRole = role;
    });
  }

  demoAction(actionName: string) {
    alert(`[Demo] Action successful: ${actionName}`);
  }
}

