import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpParams } from '@angular/common/http';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

// Interfaces for dropdown data
interface Project {
  projectId: number;
  projectName: string;
}

interface Resource {
  resourceId: number;
  resourceName: string;
  designationName: string;
}

interface PurchaseOrder {
  poId: number;
  poNumber: string;
}

interface AllocationDTO {
  allocationId?: number;
  resourceId: number;
  resourceName?: string;
  designationName?: string;
  projectId: number;
  projectName?: string;
  poId: number;
  poNumber?: string;
  allocatedHours: number;
  allocatedCost?: number;
  createdAt?: string;
  totalHoursWorked?: number;
  totalCost?: number;
}

@Component({
  selector: 'app-allocation-utilization',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    ReactiveFormsModule,
    HttpClientModule,
    MatSnackBarModule
  ],
  templateUrl: './allocation-utilization.component.html',
  styleUrl: './allocation-utilization.component.css'
})
export class AllocationUtilizationComponent implements OnInit {

  private baseUrl = 'http://localhost:8080/api';
  private http = inject(HttpClient);
    
  // Form Group
  resourceAllocationForm: FormGroup;

  // Dropdown Data
  projects: Project[] = [];
  resources: Resource[] = [];
  purchaseOrders: PurchaseOrder[] = [];

  // Utilization Table Data
  utilizationData: AllocationDTO[] = [];

  // Filters
  projectFilter: number | null = null;
  poFilter: number | null = null;

  constructor(
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    // Initialize the form
    this.resourceAllocationForm = this.fb.group({
      project: ['', Validators.required],
      resource: ['', Validators.required],
      purchaseOrder: ['', Validators.required],
      allocatedHours: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit() {
    // Fetch dropdown data
    this.loadProjects();
    this.loadResources();
    this.loadPurchaseOrders();
  }

  loadProjects() {
    this.http.get<Project[]>(`${this.baseUrl}/projects`)
      .subscribe({
        next: (data) => this.projects = data,
        error: () => this.showNotification('Failed to load projects', true)
      });
  }

  loadResources() {
    this.http.get<Resource[]>(`${this.baseUrl}/resources`)
      .subscribe({
        next: (data) => this.resources = data,
        error: () => this.showNotification('Failed to load resources', true)
      });
  }

  loadPurchaseOrders() {
    this.http.get<PurchaseOrder[]>(`${this.baseUrl}/pos`)
      .subscribe({
        next: (data) => this.purchaseOrders = data,
        error: () => this.showNotification('Failed to load purchase orders', true)
      });
  }

  // Load utilization data with filters
  loadUtilizationData() {
    if (this.projectFilter !== null && this.poFilter !== null) {
      let params = new HttpParams()
        .set('projectId', this.projectFilter.toString())
        .set('poId', this.poFilter.toString());
      
      this.http.get<AllocationDTO[]>(`${this.baseUrl}/resource/utilization`, { params })
        .subscribe({
          next: (data) => {
            this.utilizationData = data;
          },
          error: (error) => {
            this.showNotification('Failed to load utilization data: ' + 
              (error.error || error.message || 'Unknown error'), true);
            this.utilizationData = [];
          }
        });
    } else {
      // Clear data if either filter is not set
      this.utilizationData = [];
    }
  }

  // Submit resource allocation
  onSubmit() {
    if (this.resourceAllocationForm.valid) {
      const allocationDTO: AllocationDTO = {
        projectId: this.resourceAllocationForm.get('project')?.value,
        resourceId: this.resourceAllocationForm.get('resource')?.value,
        poId: this.resourceAllocationForm.get('purchaseOrder')?.value,
        allocatedHours: this.resourceAllocationForm.get('allocatedHours')?.value
      };

      this.http.post<AllocationDTO>(`${this.baseUrl}/resource/allocation`, allocationDTO)
        .subscribe({
          next: (response) => {
            this.showNotification('Resource allocated successfully');
            
            // If both filters are set and match the allocated resource, refresh the data
            if (this.projectFilter === allocationDTO.projectId && 
                this.poFilter === allocationDTO.poId) {
              this.loadUtilizationData();
            }
            
            // Reset form after submission
            this.resourceAllocationForm.reset();
          },
          error: (error) => {
            this.showNotification('Failed to allocate resource: ' + 
              (error.error || error.message || 'Unknown error'), true);
          }
        });
    }
  }

  // Filter methods
  applyProjectFilter(event: Event) {
    const target = event.target as HTMLSelectElement;
    const projectId = target.value;
    this.projectFilter = projectId ? Number(projectId) : null;
    this.loadUtilizationData();
  }
  
  applyPOFilter(event: Event) {
    const target = event.target as HTMLSelectElement;
    const poId = target.value;
    this.poFilter = poId ? Number(poId) : null;
    this.loadUtilizationData();
  }

  // Reset filters
  resetFilters() {
    this.projectFilter = null;
    this.poFilter = null;
    
    // Reset select elements in the DOM
    const projectSelect = document.getElementById('projectFilter') as HTMLSelectElement;
    const poSelect = document.getElementById('poFilter') as HTMLSelectElement;
    if (projectSelect) projectSelect.value = '';
    if (poSelect) poSelect.value = '';
    
    // Clear the utilization data
    this.utilizationData = [];
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