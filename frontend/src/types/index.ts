export type Role = 'ADMIN' | 'MANAGER' | 'COLLABORATOR';

export type VacationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
  managerId: number | null;
  managerName: string | null;
}

export interface VacationRequest {
  id: number;
  userId: number;
  userName: string;
  userEmail: string;
  managerId: number | null;
  managerName: string | null;
  startDate: string;
  endDate: string;
  status: VacationStatus;
}

export interface UserCreatePayload {
  name: string;
  email: string;
  role: Role;
  managerId?: number | null;
}

export interface VacationCreatePayload {
  userId: number;
  startDate: string;
  endDate: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}