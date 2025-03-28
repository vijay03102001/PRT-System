import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';

interface Resource {
  resourceId: number;
  resourceName: string;
  employeeId: number;
  contactDetails: string;
  designationName: string;
}

interface Designation {
  designationId: number;
  designationName: string;
}

@Component({
  selector: 'app-resource-details',
  templateUrl: './resource-details.component.html',
  styleUrls: ['./resource-details.component.css'],
  standalone: true,
  imports: [
    HttpClientModule, 
    ReactiveFormsModule,
    CommonModule
  ]
})
export class ResourceDetailsComponent implements OnInit {
  private baseUrl = 'http://localhost:8080/api';
  resources: Resource[] = [];
  designations: Designation[] = [];
  
  resourceForm: FormGroup;
  showModal = false;
  editMode = false;
  showConfirmDialog = false;
  resourceIdToDelete: number | null = null;
  currentResourceId: number | null = null;

  private http = inject(HttpClient);

  constructor(
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.resourceForm = this.fb.group({
      resourceName: ['', Validators.required],
      employeeId: ['', Validators.required],
      designationName: ['', Validators.required], // Changed to designationName
      contactDetails: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadResources();
    this.loadDesignations();
  }

  showNotification(message: string, isError: boolean = false): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: isError ? ['error-snackbar'] : ['success-snackbar']
    });
  }

  loadResources(): void {
    this.http.get<Resource[]>(`${this.baseUrl}/resources`)
      .subscribe({
        next: (data) => {
          this.resources = data;
        },
        error: (error) => {
          console.error('Error loading resources:', error);
          this.showNotification('Failed to load resources', true);
        }
      });
  }

  loadDesignations(): void {
    this.http.get<Designation[]>(`${this.baseUrl}/designations`)
      .subscribe({
        next: (data) => {
          this.designations = data;
        },
        error: (error) => {
          console.error('Error loading designations:', error);
          this.showNotification('Failed to load designations', true);
        }
      });
  }

  openModal(): void {
    this.showModal = true;
    this.editMode = false;
    this.resourceForm.reset();
  }

  closeModal(): void {
    this.showModal = false;
    this.editMode = false;
    this.currentResourceId = null;
    this.resourceForm.reset();
  }

  editResource(resource: Resource): void {
    this.editMode = true;
    this.currentResourceId = resource.resourceId;
    this.showModal = true;

    this.resourceForm.patchValue({
      resourceName: resource.resourceName,
      employeeId: resource.employeeId,
      designationName: resource.designationName,
      contactDetails: resource.contactDetails
    });
  }

 // Method to show the confirmation dialog and set the resourceId to be deleted
confirmDeleteResource(resourceId: number): void {
  this.resourceIdToDelete = resourceId; // Store the resource ID to delete
  this.showConfirmDialog = true; // Show the confirmation dialog
}

// Method to cancel the deletion
cancelDelete(): void {
  this.showConfirmDialog = false; // Close the confirmation dialog
}

 // Method to delete the resource after confirmation
deleteResource(): void {
  if (this.resourceIdToDelete !== null) {
    this.http.delete(`${this.baseUrl}/resources/${this.resourceIdToDelete}`)
      .subscribe({
        next: () => {
          // Remove the resource from the list after successful deletion
          this.resources = this.resources.filter(r => r.resourceId !== this.resourceIdToDelete);

          // Show success notification
          this.showNotification('Resource deleted successfully');
          this.showConfirmDialog = false; // Close the confirmation dialog
        },
        error: (error) => {
          // Log the error and show failure notification
          console.error('Error deleting resource:', error);
          this.showNotification('Failed to delete resource', true);
        }
      });
  }
}

  onSubmit(): void {
    if (this.resourceForm.valid) {
      const formValue = this.resourceForm.value;
      
      const resourceData = {
        resourceName: formValue.resourceName,
        employeeId: formValue.employeeId,
        designationName: formValue.designationName,  // Directly use designation name
        contactDetails: formValue.contactDetails
      };

      if (this.editMode && this.currentResourceId) {
        // Update existing resource
        this.http.put<Resource>(`${this.baseUrl}/resources/${this.currentResourceId}`, resourceData)
          .subscribe({
            next: (updatedResource) => {
              this.resources = this.resources.map(r =>
                r.resourceId === this.currentResourceId ? updatedResource : r
              );
              this.closeModal();
              this.showNotification('Resource updated successfully');
            },
            error: (error) => {
              console.error('Error updating resource:', error);
              this.showNotification('Failed to update resource', true);
            }
          });
      } else {
        // Create new resource
        this.http.post<Resource>(`${this.baseUrl}/resources`, resourceData)
          .subscribe({
            next: (newResource) => {
              this.resources = [...this.resources, newResource];
              this.closeModal();
              this.showNotification('Resource created successfully');
            },
            error: (error) => {
              console.error('Error creating resource:', error);
              this.showNotification('Failed to create resource', true);
            }
          });
      }
    }
  }
}