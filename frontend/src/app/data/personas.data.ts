import { CustomerPersona } from '../models/customer-persona.model';

export const CUSTOMER_PERSONAS: CustomerPersona[] = [
  {
    name: 'Mike',
    role: 'Business Owner',
    problems: [
      "Doesn't know today's priorities",
      'Too much manual work',
      'Wants higher profit',
      'Wants better visibility',
      'Depends heavily on experienced employees'
    ],
    goal: 'Run the business without constantly checking multiple systems.',
    avatar: '👨‍💼'
  },
  {
    name: 'Sarah',
    role: 'Sales Manager',
    problems: [
      'Responding to inquiries',
      'Checking inventory',
      'Preparing quotes',
      'Following up',
      'Losing leads'
    ],
    goal: 'Convert more inquiries into bookings faster.',
    avatar: '👩‍💼'
  },
  {
    name: 'James',
    role: 'Warehouse Manager',
    problems: [
      'Picking equipment',
      'Preparing events',
      'Tracking returns',
      'Damaged inventory',
      'Last-minute changes'
    ],
    goal: 'Know exactly what needs to be prepared and when.',
    avatar: '👨‍🏭'
  },
  {
    name: 'Emily',
    role: 'Event Customer',
    problems: [
      "Doesn't know what equipment is needed",
      'Wants quick answers',
      'Wants transparent pricing',
      'Wants easy booking',
      'Wants to know availability'
    ],
    goal: 'Plan an event without waiting for multiple emails and phone calls.',
    avatar: '👩‍🎨'
  }
];
