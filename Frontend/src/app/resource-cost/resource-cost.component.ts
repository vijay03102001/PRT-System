import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

interface Designation {
  designationId: number;
  designationName: string;
  costPerHour: number;
}

interface AuditLog {
  designationName: string;
  oldCost: number;
  newCost: number;
  updatedAt: string;
  updatedByName: string;
  updatedByDesignation: string;
}

interface Filters {
  designation: string;
  date: string;
}

@Component({
  selector: 'app-resource-cost',
  templateUrl: './resource-cost.component.html',
  styleUrls: ['./resource-cost.component.css'],
  standalone: true,
  imports: [
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    CommonModule
  ]
})
export class ResourceCostComponent implements OnInit {
  private baseUrl = 'http://localhost:8080/api';
  
  designations: Designation[] = [];
  auditLogs: AuditLog[] = [];
  
  designationForm: FormGroup;
  showModal = false;
  showConfirmDialog = false;
  editingDesignation: Designation | null = null;
  isSubmitting = false;
  
  filters: Filters = {
    designation: '',
    date: ''
  };

  designationToDelete: number | null = null;
  private http = inject(HttpClient);

  constructor(
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.designationForm = this.fb.group({
      name: ['', Validators.required],
      dailyCost: ['', [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.loadDesignations();
    this.loadAuditLogs();
  }

  loadDesignations(): void {
    this.http.get<Designation[]>(`${this.baseUrl}/designations`)
      .subscribe({
        next: (data) => this.designations = data,
        error: () => this.showNotification('Failed to load designations', true)
      });
  }

  loadAuditLogs(): void {
    this.http.get<AuditLog[]>(`${this.baseUrl}/designations/audit-logs`)
      .subscribe({
        next: (data) => this.auditLogs = data,
        error: () => this.showNotification('Failed to load audit logs', true)
      });
  }
  
  applyFilters(): void {
    const params = new URLSearchParams();
    if (this.filters.designation) params.append('designation', this.filters.designation);
    if (this.filters.date) params.append('date', this.filters.date);
    
    this.http.get<AuditLog[]>(`${this.baseUrl}/designations/audit-logs?${params.toString()}`)
      .subscribe({
        next: (data) => this.auditLogs = data,
        error: () => this.showNotification('Failed to load audit logs', true)
      });
  }
  
  openDesignationModal(): void {
    this.showModal = true;
    this.editingDesignation = null;
    this.designationForm.reset();
  }

  closeModal(): void {
    this.showModal = false;
    this.editingDesignation = null;
    this.designationForm.reset();
  }

  editDesignation(designation: Designation): void {
    this.editingDesignation = designation;
    this.showModal = true;
    this.designationForm.patchValue({
      name: designation.designationName,
      dailyCost: designation.costPerHour
    });
  }

  deleteDesignation(id: number): void {
    this.designationToDelete = id;
    this.showConfirmDialog = true;
  }

  cancelDelete(): void {
    this.showConfirmDialog = false;
    this.designationToDelete = null;
  }

  confirmDelete(): void {
    if (this.designationToDelete) {
      this.http.delete(`${this.baseUrl}/designations/${this.designationToDelete}`)
        .subscribe({
          next: () => {
            this.designations = this.designations.filter(d => d.designationId !== this.designationToDelete);
            this.showNotification('Designation deleted successfully');
          },
          error: () => this.showNotification('Failed to delete designation', true),
          complete: () => {
            this.showConfirmDialog = false;
            this.designationToDelete = null;
          }
        });
    }
  }

  saveDesignation(): void {
    if (this.designationForm.valid) {
      this.isSubmitting = true;
      const formValue = this.designationForm.value;
      
      if (this.editingDesignation) {
        this.http.put<Designation>(
          `${this.baseUrl}/designations/${this.editingDesignation.designationId}`,
          formValue
        ).subscribe({
          next: (updatedDesignation) => {
            this.designations = this.designations.map(d => 
              d.designationId === this.editingDesignation?.designationId ? updatedDesignation : d);
            this.closeModal();
            this.showNotification('Designation updated successfully');
          },
          error: () => this.showNotification('Failed to update designation', true),
          complete: () => this.isSubmitting = false
        });
      } else {
        this.http.post<Designation>(`${this.baseUrl}/designations`, formValue)
          .subscribe({
            next: (newDesignation) => {
              this.designations = [...this.designations, newDesignation];
              this.closeModal();
              this.showNotification('Designation created successfully');
            },
            error: () => this.showNotification('Failed to create designation', true),
            complete: () => this.isSubmitting = false
          });
      }
    }
  }

  showError(fieldName: string): boolean {
    const field = this.designationForm.get(fieldName);
    return field ? field.invalid && (field.dirty || field.touched) : false;
  }

  private showNotification(message: string, isError: boolean = false): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: isError ? ['error-snackbar'] : ['success-snackbar']
    });
  }
}
