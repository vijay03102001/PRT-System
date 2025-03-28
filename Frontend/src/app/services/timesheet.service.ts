import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { TimesheetEntry } from '../models/timesheet-entry.model';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'any',
})
export class TimesheetService {
  private apiUrl = 'http://localhost:8080/api/entry';
  private timesheetSubject = new BehaviorSubject<TimesheetEntry[]>([]);

  constructor(private http: HttpClient, private authService: AuthService) {
    this.loadTimesheets();
  }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }

  private loadTimesheets() {
    this.http.get<TimesheetEntry[]>(this.apiUrl, { headers: this.getAuthHeaders() }).subscribe(
      data => this.timesheetSubject.next(data),
      error => console.error('Error loading timesheets:', error)
    );
  }

  submitTimesheet(entry: TimesheetEntry): Observable<TimesheetEntry> {
    return this.http.post<TimesheetEntry>(this.apiUrl, entry, { headers: this.getAuthHeaders() }).pipe(
      tap(() => this.loadTimesheets())
    );
  }

  getPendingTimesheets(): Observable<TimesheetEntry[]> {
    return this.http.get<TimesheetEntry[]>(`${this.apiUrl}/pending`, { headers: this.getAuthHeaders() });
  }

  getAllTimesheets(): Observable<TimesheetEntry[]> {
    return this.http.get<TimesheetEntry[]>(this.apiUrl, { headers: this.getAuthHeaders() });
  }

  approveTimesheet(id: number): Observable<TimesheetEntry> {
    return this.http.put<TimesheetEntry>(`${this.apiUrl}/${id}/approve`, {}, { headers: this.getAuthHeaders() }).pipe(
      tap(() => this.loadTimesheets())
    );
  }

  rejectTimesheet(id: number, reason: string): Observable<TimesheetEntry> {
    return this.http.put<TimesheetEntry>(`${this.apiUrl}/${id}/reject`, { reason }, { headers: this.getAuthHeaders() }).pipe(
      tap(() => this.loadTimesheets())
    );
  }

  toggleFreeze(id: number): Observable<TimesheetEntry> {
    return this.http.put<TimesheetEntry>(`${this.apiUrl}/${id}/toggle-freeze`, {}, { headers: this.getAuthHeaders() }).pipe(
      tap(() => this.loadTimesheets())
    );
  }
}
