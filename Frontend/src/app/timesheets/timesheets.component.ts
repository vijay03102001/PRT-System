import { Component, OnInit, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

interface Timesheet {
  timesheetId: number;
  resourceId: number;
  projectId: number;
  poId: number;
  workDate: string;
  hoursWorked: number;
  cost: number;
  status: string;
  resourceName: string;
  projectName: string;
  poNumber: string;
  costPerHour: number;
}

interface NewTimesheet {
  resourceId: number;
  projectId: number;
  poId: number;
  workDate: string;
  hoursWorked: number;
}

interface Project {
  projectId: number;
  projectName: string;
}

interface PurchaseOrder {
  poId: number;
  poNumber: string;
}

interface Resource {
  resourceId: number;
  resourceName: string;
  costPerHour: number;
}

@Component({
  selector: 'app-timesheets',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatSnackBarModule,
    HttpClientModule
  ],
  providers: [CurrencyPipe],
  templateUrl: './timesheets.component.html',
  styleUrls: ['./timesheets.component.css']
})
export class TimesheetsComponent implements OnInit {
  private baseUrl = 'http://localhost:8080/api';
  private http = inject(HttpClient);

  timesheetForm: FormGroup;
  pendingTimesheets: Timesheet[] = [];
  projects: Project[] = [];
  purchaseOrders: PurchaseOrder[] = [];
  resources: Resource[] = [];

  isFrozen = false;
  selectedResource: Resource | null = null;

  constructor(
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.timesheetForm = this.fb.group({
      workDate: ['', Validators.required],
      resourceId: ['', Validators.required],
      projectId: ['', Validators.required],
      poId: ['', Validators.required],
      hoursWorked: [null, [Validators.required, Validators.min(0), Validators.max(24)]]
    });

    this.timesheetForm.get('resourceId')?.valueChanges.subscribe(resourceId => {
      this.updateSelectedResource(resourceId);
    });
  }

  ngOnInit(): void {
    this.loadResources();
    this.loadProjects();
    this.loadPurchaseOrders();
    this.loadPendingTimesheets();
  }

  loadResources(): void {
    this.http.get<Resource[]>(`${this.baseUrl}/resources`)
      .subscribe({
        next: (data) => this.resources = data,
        error: () => this.showNotification('Failed to load resources', true)
      });
  }

  updateSelectedResource(resourceId: number): void {
    this.selectedResource = this.resources.find(r => r.resourceId === resourceId) || null;
  }

  loadProjects(): void {
    this.http.get<Project[]>(`${this.baseUrl}/projects`)
      .subscribe({
        next: (data) => this.projects = data,
        error: () => this.showNotification('Failed to load projects', true)
      });
  }

  loadPurchaseOrders(): void {
    this.http.get<PurchaseOrder[]>(`${this.baseUrl}/pos`)
      .subscribe({
        next: (data) => this.purchaseOrders = data,
        error: () => this.showNotification('Failed to load purchase orders', true)
      });
  }

  loadPendingTimesheets(): void {
    this.http.get<Timesheet[]>(`${this.baseUrl}/timesheets`)
      .subscribe({
        next: (data) => this.pendingTimesheets = data,
        error: () => this.showNotification('Failed to load pending timesheets', true)
      });
  }

  onSubmit(): void {
    if (this.timesheetForm.valid && !this.isFrozen && this.selectedResource) {
      const formValue = this.timesheetForm.value;
      const newTimesheet: NewTimesheet = {
        resourceId: formValue.resourceId,
        projectId: formValue.projectId,
        poId: formValue.poId,
        workDate: formValue.workDate,
        hoursWorked: formValue.hoursWorked
      };

      this.http.post<Timesheet>(`${this.baseUrl}/timesheets`, newTimesheet)
        .subscribe({
          next: (response) => {
            this.pendingTimesheets.push(response);
            this.timesheetForm.reset();
            this.showNotification('Timesheet submitted successfully');
          },
          error: () => this.showNotification('Failed to submit timesheet', true)
        });
    }
  }

  approveTimesheet(id: number): void {
    this.http.put(`${this.baseUrl}/timesheets/${id}/approve`, {})
      .subscribe({
        next: () => {
          const timesheet = this.pendingTimesheets.find(t => t.timesheetId === id);
          if (timesheet) {
            timesheet.status = 'Approved';
            this.showNotification('Timesheet approved successfully');
          }
        },
        error: () => this.showNotification('Failed to approve timesheet', true)
      });
  }

  rejectTimesheet(id: number): void {
    this.http.put(`${this.baseUrl}/timesheets/${id}/reject`, {})
      .subscribe({
        next: () => {
          const timesheet = this.pendingTimesheets.find(t => t.timesheetId === id);
          if (timesheet) {
            timesheet.status = 'Rejected';
            this.showNotification('Timesheet rejected successfully');
          }
        },
        error: () => this.showNotification('Failed to reject timesheet', true)
      });
  }

  toggleFreeze(): void {
    this.isFrozen = !this.isFrozen;
    this.showNotification(
      this.isFrozen ?
      'Timesheet submissions are now frozen' :
      'Timesheet submissions are now enabled'
    );
  }

  private showNotification(message: string, isError = false): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: isError ? ['error-snackbar'] : ['success-snackbar']
    });
  }
}
