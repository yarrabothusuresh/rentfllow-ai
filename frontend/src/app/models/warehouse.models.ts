export interface Warehouse {
  id: string;
  tenantId?: string;
  code: string;
  name: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  capacity?: number;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}
