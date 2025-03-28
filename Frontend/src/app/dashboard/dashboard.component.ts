import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Chart } from 'chart.js/auto';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';


interface DashboardSummary {
  totalBudget: number;
  totalPOValue: number;
  totalUtilized: number;
  totalRemaining: number;
}

interface Project {
  projectId: string;
  projectName: string;
  projectDescription: string;
  projectBudget: number;
  utilizedAmount: number;
  remainingBalance: number;
  totalPOs: number;
}

interface PurchaseOrder {
  poId: string;
  poNumber: string;
  value: number;
  projectId: string;
  poUtilized: number;
  poBalance: number;
}

interface Resource {
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

interface Designation {
  designationId: number;
  designationName: string;
  costPerHour: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule,
    ReactiveFormsModule,
    MatSnackBarModule,
    HttpClientModule],
  providers: [CurrencyPipe],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  private baseUrl = 'http://localhost:8080/api';
  private pieChart: Chart | undefined;
  private barChart: Chart | undefined;

  // Dashboard Summary
  dashboardSummary: DashboardSummary = {
    totalBudget: 500000,
    totalPOValue: 100000,
    totalUtilized: 25000,
    totalRemaining: 475000
  };

  // Data arrays
  projects: Project[] = [];
  purchaseOrders: PurchaseOrder[] = [];
  resources: Resource[] = [];
  filteredResources: Resource[] = [];
  designations: Designation[] = [];

  // UI state
  selectedProject: string = '';
  activeTab: string = 'projects';
  selectedDesignation: string = '';
  loading = {
    summary: false,
    projects: false,
    pos: false
  };

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadDashboardSummary();
    this.loadAllProjects();
    this.loadAllPurchaseOrders();
    this.loadResources();
  }

  ngOnDestroy() {
    this.pieChart?.destroy();
    this.barChart?.destroy();
  }

  // Data loading methods
  private loadDashboardSummary() {
    this.loading.summary = true;
    this.http.get<DashboardSummary>(`${this.baseUrl}/dashboard/summary`).subscribe({
      next: (data) => {
        this.dashboardSummary = data;
        this.initializePieChart();
        this.loading.summary = false;
      },
      error: (error) => {
        console.error('Error loading dashboard summary:', error);
        this.loading.summary = false;
      }
    });
  }

  private loadAllProjects() {
    this.loading.projects = true;
    this.http.get<Project[]>(`${this.baseUrl}/projects`).subscribe({
      next: (data) => {
        this.projects = data;
        this.initializeBarChart();
        this.loading.projects = false;
      },
      error: (error) => {
        console.error('Error loading projects:', error);
        this.loading.projects = false;
      }
    });
  }

  private loadAllPurchaseOrders() {
    this.loading.pos = true;
    this.http.get<PurchaseOrder[]>(`${this.baseUrl}/pos`).subscribe({
      next: (data) => {
        this.purchaseOrders = data;
        this.loading.pos = false;
      },
      error: (error) => {
        console.error('Error loading POs:', error);
        this.loading.pos = false;
      }
    });
  }

  private loadResources() {
    this.http.get<Resource[]>(`${this.baseUrl}/resources`).subscribe({
      next: (data) => {
        this.resources = data;
        this.filteredResources = data;
      },
      error: (error) => console.error('Error loading resources:', error)
    });
  }

  // Chart initialization
  private initializePieChart() {
    const ctx = document.getElementById('budgetChart') as HTMLCanvasElement;
    if (ctx) {
      this.pieChart = new Chart(ctx, {
        type: 'pie',
        data: {
          labels: [ 'Utilized', 'Remaining'],
          datasets: [{
            data: [

              this.dashboardSummary.totalUtilized,
              this.dashboardSummary.totalRemaining
            ],
            backgroundColor: [ '#005F73', '#94D2BD'],
            borderWidth: 2
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: {
              position: 'bottom',
              labels: {
                padding: 20,
                font: { size: 12 }
              }
            },
            title: {
              display: true,
              text: 'Budget Overview',
              font: { size: 16, weight: 'bold' }
            }
          }
        }
      });
    }
  }

  private initializeBarChart() {
    const ctx = document.getElementById('utilizationChart') as HTMLCanvasElement;
    if (ctx) {
      this.barChart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: this.projects.map(p => p.projectName),
          datasets: [
            {
              label: 'Project Budget',
              data: this.projects.map(p => p.projectBudget),
              backgroundColor: '#0A9396'
            },
            {
              label: 'Utilized Amount',
              data: this.projects.map(p => p.utilizedAmount),
              backgroundColor: '#005F73'
            },
            {
              label: 'Remaining Balance',
              data: this.projects.map(p => p.remainingBalance),
              backgroundColor: '#94D2BD'
            }
          ]
        },
        options: {
          responsive: true,
          plugins: {
            legend: {
              position: 'bottom',
              labels: { font: { size: 12 } }
            },
            title: {
              display: true,
              text: 'Project-wise Budget Analysis',
              font: { size: 16, weight: 'bold' }
            }
          },
          scales: {
            y: {
              beginAtZero: true,
              grid: { color: 'rgba(0, 95, 115, 0.1)' }
            },
            x: { grid: { display: false } }
          }
        }
      });
    }
  }

  // Filtering methods
  get filteredProjects(): Project[] {
    return this.selectedProject
      ? this.projects.filter(project => project.projectId === this.selectedProject)
      : this.projects;
  }

  get filteredPOs(): PurchaseOrder[] {
    return this.selectedProject
      ? this.purchaseOrders.filter(po => po.projectId === this.selectedProject)
      : this.purchaseOrders;
  }

  onProjectSelect(event: any): void {
    this.selectedProject = event.target.value;
    if (this.selectedProject) {
      this.loadProjectDetails(this.selectedProject);
    }
  }

  private loadProjectDetails(projectId: string) {
    this.http.get<Project>(`${this.baseUrl}/projects/${projectId}`).subscribe({
      next: (project) => {
        this.http.get<PurchaseOrder[]>(`${this.baseUrl}/projects/${projectId}/pos`).subscribe({
          next: (pos) => {
            this.purchaseOrders = pos;
          },
          error: (error) => console.error('Error loading project POs:', error)
        });
      },
      error: (error) => console.error('Error loading project details:', error)
    });
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  applyDesignationFilter(): void {
    if (this.selectedDesignation) {
      this.filteredResources = this.resources.filter(
        resource => resource.designationName === this.selectedDesignation
      );
    } else {
      this.filteredResources = this.resources;
    }
  }
}
