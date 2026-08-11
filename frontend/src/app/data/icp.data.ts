import { IcpPainPoint } from '../models/icp-pain-point.model';

export const ICP_PAIN_POINTS: IcpPainPoint[] = [
  {
    problem: 'Too Many Inquiries',
    whyItMatters: 'Leads arrive from email, phone, website, and SMS, making it difficult for reps to capture and qualify them all.',
    rentflowSolution: 'AI Sales Agent qualifications and automatic routing to centralize incoming leads.'
  },
  {
    problem: 'Manual Quoting',
    whyItMatters: 'Sales staff spends 45–90 minutes calculating item prices, taxes, custom delivery fees, and estimated setups.',
    rentflowSolution: 'AI Quote Generator creates margin-optimized quote drafts in under 10 minutes.'
  },
  {
    problem: 'Inventory Uncertainty',
    whyItMatters: 'Reps send quotes for events without real-time assurance of date-based item availability, leading to double-booking.',
    rentflowSolution: 'Real-time date-specific AI availability checks prevent overbooking before confirming a contract.'
  },
  {
    problem: 'Lost Follow-ups',
    whyItMatters: 'Quotes sit unanswered because reps forget to follow up, resulting in cold leads and lost revenue.',
    rentflowSolution: 'Automated AI sales follow-ups prompt customers at optimal intervals to close the booking.'
  },
  {
    problem: 'Operational Complexity',
    whyItMatters: 'Transitioning from bookings to warehouse loading, delivery routes, and returns is uncoordinated and error-prone.',
    rentflowSolution: 'Connected rental workflow links confirmed bookings directly with digital loading lists and dispatch boards.'
  },
  {
    problem: 'Profitability Uncertainty',
    whyItMatters: 'Event rental companies see high revenue but do not know the actual net profit margin of an event beforehand.',
    rentflowSolution: 'AI margin estimator provides pre-booking visibility into labor, delivery, setup, and item cost margins.'
  },
  {
    problem: 'Late Payments & Bad Debt',
    whyItMatters: 'Chasing clients for deposits and balances requires constant manual email reminders and phone calls.',
    rentflowSolution: 'Automated deposit workflows and integrated payment links ensure cards are charged on schedule.'
  },
  {
    problem: 'Unknown Asset Utilization',
    whyItMatters: 'Owners cannot easily identify which inventory items are underutilized or which events are most profitable.',
    rentflowSolution: 'AI Business Copilot answers natural language questions using real-time utilization data.'
  }
];

export const ICP_COMPARISONS = [
  {
    traditional: 'Manual quote creation',
    rentflow: 'AI-assisted quote creation'
  },
  {
    traditional: 'Reactive follow-up',
    rentflow: 'AI sales follow-up'
  },
  {
    traditional: 'Inventory lookup',
    rentflow: 'AI availability intelligence'
  },
  {
    traditional: 'Static reports',
    rentflow: 'AI business insights'
  },
  {
    traditional: 'Separate tools',
    rentflow: 'Connected rental workflow'
  }
];
