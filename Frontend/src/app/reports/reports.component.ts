import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormBuilder, FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';

interface ApiResponse {
  project: {
    projectId: number;
    projectName: string;
  };
  purchaseOrders: Array<{
    poId: number;
    poNumber: string;
    value: number;
    poUtilized: number;
    poBalance: number;
  }>;
  resources: Array<{
    resourceName: string;
    designationName: string;
  }>;
}

interface DetailedReport {
  poId: string;
  poNumber: string;
  projectId: string;
  projectName: string;
  value: number;
  poUtilized: number;
  poBalance: number;
  resourceName: string;
  designationName: string;
}


interface DetailedReport {
  poId: string;
  poNumber: string;
  projectId: string;
  projectName: string;
  value: number;
  poUtilized: number;
  poBalance: number;
  resourceName: string;
  designationName: string;
}

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    MatSnackBarModule,
    MatButtonModule
  ],
  providers: [CurrencyPipe]
})
export class ReportsComponent implements OnInit {
  private baseUrl = 'http://localhost:8080/api';
  private http = inject(HttpClient);

  detailedReports: DetailedReport[] = [];

  constructor(
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private currencyPipe: CurrencyPipe
  ) {}

  ngOnInit(): void {
    this.loadDetailedReports(); // Initially load reports without any filters
  }

  loadDetailedReports(): void {
    const url = `${this.baseUrl}/reports/all`;

    this.http.get<ApiResponse[]>(url).subscribe({
      next: (data) => {
        console.log("API Response:", data);
        this.detailedReports = this.flattenData(data);
      },
      error: () => this.showNotification('Failed to load detailed reports', true)
    });
  }

  private flattenData(data: ApiResponse[]): DetailedReport[] {
    const flattenedData: DetailedReport[] = [];

    data.forEach(item => {
      // For each purchase order, create an entry with each resource
      item.purchaseOrders.forEach(po => {
        // If there are no resources, still show the PO with empty resource fields
        if (item.resources.length === 0) {
          flattenedData.push({
            poId: po.poId.toString(),
            poNumber: po.poNumber,
            projectId: item.project.projectId.toString(),
            projectName: item.project.projectName,
            resourceName: '',
            designationName: '',
            value: po.value,
            poUtilized: po.poUtilized,
            poBalance: po.poBalance
          });
        } else {
          // Create an entry for each resource
          item.resources.forEach(resource => {
            flattenedData.push({
              poId: po.poId.toString(),
              poNumber: po.poNumber,
              projectId: item.project.projectId.toString(),
              projectName: item.project.projectName,
              resourceName: resource.resourceName,
              designationName: resource.designationName,
              value: po.value,
              poUtilized: po.poUtilized,
              poBalance: po.poBalance
            });
          });
        }
      });
    });

    return flattenedData;
  }

  exportToExcel(): void {
    let url = `${this.baseUrl}/reports/export`;
    const params: string[] = [];

    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: (blob) => this.downloadFile(blob, 'Reports.xlsx'),
      error: () => this.showNotification('Failed to export Excel report', true)
    });
  }

  private downloadFile(blob: Blob, filename: string): void {
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(link.href);
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
