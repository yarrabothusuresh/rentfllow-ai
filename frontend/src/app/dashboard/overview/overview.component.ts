import { Component } from '@angular/core';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss'
})
export class OverviewComponent {
  demoAction(actionName: string) {
    alert(`[Demo] Action successful: ${actionName}`);
  }
}
