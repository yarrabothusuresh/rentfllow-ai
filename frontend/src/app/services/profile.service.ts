import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { BusinessProfile } from '../models/business-profile.model';
import { INITIAL_BUSINESS_PROFILE } from '../data/business-profile.data';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private apiUrl = '/api/business/profile';

  constructor(private http: HttpClient) {}

  getProfile(): Observable<BusinessProfile> {
    return this.http.get<any>(this.apiUrl).pipe(
      map(data => {
        return {
          name: data.name || INITIAL_BUSINESS_PROFILE.name,
          city: data.city || INITIAL_BUSINESS_PROFILE.city,
          state: data.state || INITIAL_BUSINESS_PROFILE.state,
          country: data.country || INITIAL_BUSINESS_PROFILE.country,
          businessType: data.businessType || INITIAL_BUSINESS_PROFILE.businessType,
          employees: data.employees !== undefined ? data.employees : INITIAL_BUSINESS_PROFILE.employees,
          locations: data.locations !== undefined ? data.locations : INITIAL_BUSINESS_PROFILE.locations,
          productsCount: data.products !== undefined ? data.products : INITIAL_BUSINESS_PROFILE.productsCount,
          inventoryUnitsCount: INITIAL_BUSINESS_PROFILE.inventoryUnitsCount
        } as BusinessProfile;
      }),
      catchError(error => {
        console.warn('Backend profile API failed, falling back to mock data', error);
        return of(INITIAL_BUSINESS_PROFILE);
      })
    );
  }
}
