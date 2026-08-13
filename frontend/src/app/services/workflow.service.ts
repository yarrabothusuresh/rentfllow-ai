import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export type WorkflowStageKey =
  | 'INQUIRY' | 'LEAD' | 'QUOTE' | 'BOOKING' | 'INVENTORY' | 'WAREHOUSE'
  | 'DELIVERY' | 'EVENT' | 'PICKUP' | 'RETURN' | 'INSPECTION' | 'PAYMENT' | 'COMPLETED';

export type ViewMode = 'INTERNAL' | 'CUSTOMER';

export interface WorkflowStageItem {
  name: string;
  stageKey: WorkflowStageKey;
  status: 'COMPLETED' | 'CURRENT' | 'PENDING';
  role: string;
  description: string;
  date: string;
  icon: string;
}

export interface DemoScenario {
  customerName: string;
  customerLocation: string;
  eventType: string;
  eventDate: string;
  guestCount: number;
  companyName: string;
  products: string[];
  estimatedRental: number;
  deliverySetup: number;
  totalEstimated: number;
  estimatedMargin: number;
  estimatedCost: number;
}

export interface WorkflowData {
  bookingId: string;
  currentStage: WorkflowStageKey;
  progress: number;
  stages: WorkflowStageItem[];
  demoScenario: DemoScenario;
}

@Injectable({
  providedIn: 'root'
})
export class WorkflowService {
  private viewModeSubject = new BehaviorSubject<ViewMode>('INTERNAL');
  viewMode$ = this.viewModeSubject.asObservable();

  private workflowDataSubject = new BehaviorSubject<WorkflowData>(this.getInitialMockData());
  workflowData$ = this.workflowDataSubject.asObservable();

  constructor(private http: HttpClient) {
    this.fetchWorkflow();
  }

  getViewMode(): ViewMode {
    return this.viewModeSubject.value;
  }

  setViewMode(mode: ViewMode) {
    this.viewModeSubject.next(mode);
  }

  fetchWorkflow(bookingId: string = 'demo-booking-001') {
    this.http.get<WorkflowData>(`/api/workflows/rental/${bookingId}`).pipe(
      catchError(err => {
        console.warn('Backend workflow endpoint unavailable, using mock data.', err);
        return of(this.workflowDataSubject.value);
      })
    ).subscribe(data => {
      if (data && data.stages) {
        // Enrich stage objects with icons if needed
        data.stages = data.stages.map(s => ({
          ...s,
          icon: this.getIconForStage(s.stageKey)
        }));
        this.workflowDataSubject.next(data);
      }
    });
  }

  advanceWorkflow(bookingId: string = 'demo-booking-001') {
    const current = this.workflowDataSubject.value;
    const stages = current.stages;
    const currentIdx = stages.findIndex(s => s.status === 'CURRENT');
    
    let nextIdx = 0;
    if (currentIdx !== -1 && currentIdx < stages.length - 1) {
      nextIdx = currentIdx + 1;
    }

    const nextStageKey = stages[nextIdx].stageKey;

    this.http.post<any>(`/api/workflows/rental/${bookingId}/advance`, {
      targetStage: nextStageKey,
      reason: 'User triggered demo workflow advancement',
      requestedByRole: 'SALES'
    }).pipe(
      catchError(err => {
        console.warn('Backend advance endpoint unavailable, performing client-side update.', err);
        return of(null);
      })
    ).subscribe(() => {
      this.updateClientWorkflowState(nextIdx);
    });
  }

  resetWorkflow() {
    this.updateClientWorkflowState(2); // Reset back to Quote stage
  }

  private updateClientWorkflowState(currentIdx: number) {
    const current = { ...this.workflowDataSubject.value };
    const total = current.stages.length;

    current.stages = current.stages.map((stage, idx) => {
      let status: 'COMPLETED' | 'CURRENT' | 'PENDING';
      if (idx < currentIdx) {
        status = 'COMPLETED';
      } else if (idx === currentIdx) {
        status = 'CURRENT';
      } else {
        status = 'PENDING';
      }
      return { ...stage, status };
    });

    current.currentStage = current.stages[currentIdx].stageKey;
    current.progress = Math.round(((currentIdx + 1) / total) * 100);

    this.workflowDataSubject.next(current);
  }

  private getIconForStage(stageKey: WorkflowStageKey): string {
    switch (stageKey) {
      case 'INQUIRY': return '💬';
      case 'LEAD': return '🎯';
      case 'QUOTE': return '📄';
      case 'BOOKING': return '✍️';
      case 'INVENTORY': return '📦';
      case 'WAREHOUSE': return '🏭';
      case 'DELIVERY': return '🚚';
      case 'EVENT': return '🎉';
      case 'PICKUP': return '🚛';
      case 'RETURN': return '🔄';
      case 'INSPECTION': return '🔍';
      case 'PAYMENT': return '💳';
      case 'COMPLETED': return '✨';
      default: return '📍';
    }
  }

  private getInitialMockData(): WorkflowData {
    const stages: WorkflowStageItem[] = [
      { name: '01 Inquiry', stageKey: 'INQUIRY', status: 'COMPLETED', role: 'Sales', description: 'Customer Emily Brown submitted Dallas wedding inquiry.', date: 'Sep 10, 2026', icon: '💬' },
      { name: '02 Lead', stageKey: 'LEAD', status: 'COMPLETED', role: 'Sales', description: 'Lead qualified by Sales; budget & guest count verified.', date: 'Sep 11, 2026', icon: '🎯' },
      { name: '03 Quote', stageKey: 'QUOTE', status: 'CURRENT', role: 'Sales', description: 'Quote #Q-8492 ($6,480) sent to Emily Brown.', date: 'Sep 12, 2026', icon: '📄' },
      { name: '04 Booking', stageKey: 'BOOKING', status: 'PENDING', role: 'Sales', description: 'Awaiting customer signature & 25% deposit.', date: 'Scheduled Sep 13', icon: '✍️' },
      { name: '05 Inventory', stageKey: 'INVENTORY', status: 'PENDING', role: 'Warehouse', description: 'Reserving 250 Chiavari chairs, 25 tables, 25 linens.', date: 'Scheduled Sep 14', icon: '📦' },
      { name: '06 Warehouse', stageKey: 'WAREHOUSE', status: 'PENDING', role: 'Warehouse', description: 'Warehouse pick & pack staging for truck load.', date: 'Scheduled Sep 18', icon: '🏭' },
      { name: '07 Delivery', stageKey: 'DELIVERY', status: 'PENDING', role: 'Driver', description: 'Truck delivery to Dallas Event Pavilion.', date: 'Scheduled Sep 20 09:00 AM', icon: '🚚' },
      { name: '08 Event', stageKey: 'EVENT', status: 'PENDING', role: 'Operations', description: 'Wedding reception ceremony in progress.', date: 'Scheduled Sep 20 03:00 PM', icon: '🎉' },
      { name: '09 Pickup', stageKey: 'PICKUP', status: 'PENDING', role: 'Driver', description: 'Driver pickup from venue after event end.', date: 'Scheduled Sep 21 08:00 AM', icon: '🚛' },
      { name: '10 Return', stageKey: 'RETURN', status: 'PENDING', role: 'Warehouse', description: 'Unload return items into warehouse receiving bay.', date: 'Scheduled Sep 21 11:00 AM', icon: '🔄' },
      { name: '11 Payment', stageKey: 'PAYMENT', status: 'PENDING', role: 'Finance', description: 'Final invoice settlement & damage deposit release.', date: 'Scheduled Sep 22', icon: '💳' },
      { name: '12 Completed', stageKey: 'COMPLETED', status: 'PENDING', role: 'Management', description: 'Rental archived with 54.9% gross margin achieved.', date: 'Scheduled Sep 23', icon: '✨' }
    ];

    return {
      bookingId: 'demo-booking-001',
      currentStage: 'QUOTE',
      progress: 25,
      stages,
      demoScenario: {
        customerName: 'Emily Brown',
        customerLocation: 'Dallas, Texas',
        eventType: 'Wedding',
        eventDate: 'September 20, 2026',
        guestCount: 250,
        companyName: 'Evergreen Event Rentals',
        products: [
          '250 Chiavari Chairs',
          '25 Round Tables',
          '25 White Table Linens'
        ],
        estimatedRental: 4850.00,
        deliverySetup: 1150.00,
        totalEstimated: 6480.00,
        estimatedMargin: 54.9,
        estimatedCost: 2920.00
      }
    };
  }
}
